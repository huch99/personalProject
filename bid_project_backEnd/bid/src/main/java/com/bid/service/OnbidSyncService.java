package com.bid.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.bid.dto.response.TenderResponseDTO;
import com.bid.entity.Tender;
import com.bid.repository.TenderRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OnbidSyncService implements ApplicationRunner {

	private final RestTemplate restTemplate;
	private final TenderRepository tenderRepository;
	private final OnbidApiParser onbidApiParser;
	private final TransactionTemplate transactionTemplate;
	private final Executor onbidApiExecutor;

	public OnbidSyncService(RestTemplate restTemplate, TenderRepository tenderRepository, OnbidApiParser onbidApiParser,
			TransactionTemplate transactionTemplate, @Qualifier("onbidApiExecutor") Executor onbidApiExecutor) {
		this.restTemplate = restTemplate;
		this.tenderRepository = tenderRepository;
		this.onbidApiParser = onbidApiParser;
		this.transactionTemplate = transactionTemplate;
		this.onbidApiExecutor = onbidApiExecutor;
	}

	@Value("${onbid.api.base-url}")
	private String onbidApiBaseUrl;

	@Value("${onbid.api.service-key}")
	private String onbidApiServiceKey;

	private static final int MAX_ONBID_API_NUM_OF_ROWS = 99; // API 한 번 호출 시 가져올 최대 건수
	private static final int INITIAL_FAST_SYNC_PAGES = 2;

	// ✅ 동기화 상태 추적 (로딩 중 사용자에게 알리기 위함)
	private boolean isSyncing = false;

	public boolean isSyncing() {
		return isSyncing;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("Application started. Initiating initial Onbid Tender synchronization...");
		// ✅ 첫 동기화는 빠르게 필수 데이터만 가져옵니다.
		performFastSync();
		log.info("Initial fast Onbid Tender synchronization completed. Full sync will run in background.");

		// ✅ 나머지 전체 동기화는 별도의 비동기 스레드에서 실행
		performFullSyncInBackground();
	}

	@Scheduled(fixedRate = 3600000, initialDelay = 3600000)
	public void syncOnbidTendersScheduled() { // 메서드 이름 변경 (run()과 충돌 방지)
		log.info("Starting scheduled Onbid Tender synchronization at {}", LocalDateTime.now());
		if (!isSyncing) { // 현재 동기화 중이 아니라면
			isSyncing = true;
			performFullSyncInBackground(); // 스케줄된 동기화도 비동기로
		} else {
			log.info("Skipping scheduled sync, another sync is already in progress.");
		}
	}

	private void performFastSync() {
		log.info("Starting fast sync for initial {} pages...", INITIAL_FAST_SYNC_PAGES);
		List<TenderResponseDTO> fastSyncTenders = fetchOnbidDataPages(1, INITIAL_FAST_SYNC_PAGES);

		transactionTemplate.execute(status -> { // 트랜잭션 관리
			return saveOrUpdateTenders(fastSyncTenders);
		});
		log.info("Fast sync saved/updated {} tenders.", fastSyncTenders.size());
	}

	@Async("onbidApiExecutor")
	@Transactional(readOnly = false) // @Async와 @Transactional을 함께 사용 시 주의: 새로운 트랜잭션에서 실행됨
	public void performFullSyncInBackground() {
		if (isSyncing) {
			log.info("Full sync is already in progress. Skipping new request.");
			return;
		}
		isSyncing = true;
		log.info("Starting full sync in background...");
		long fullSyncStartTime = System.currentTimeMillis();

		int totalCount = 0;
		int totalPages = 1;

		UriComponentsBuilder initialUriBuilder = UriComponentsBuilder.fromUriString(onbidApiBaseUrl)
				.queryParam("serviceKey", onbidApiServiceKey).queryParam("pageNo", 1)
				.queryParam("numOfRows", 10000);

		try {
			ResponseEntity<String> initialResponseEntity = restTemplate
					.getForEntity(initialUriBuilder.encode().build().toUri(), String.class);
			if (initialResponseEntity.getStatusCode().is2xxSuccessful() && initialResponseEntity.getBody() != null) {
				String xmlResponse = initialResponseEntity.getBody();
			    log.info(">>>> Raw XML Response from Onbid API (initial): {}", xmlResponse);
				
				OnbidApiParser.TenderListResult initialParsedResult = onbidApiParser
						.parseXmlToTenderDtosAndCount(initialResponseEntity.getBody());
				totalCount = initialParsedResult.getTotalCount();
				totalPages = (int) Math.ceil((double) totalCount / 10000);
				log.info("Onbid API Total Count: {}. Calculated Total Pages: {} (based on {} rows/page)", totalCount,
						totalPages, 10000);
			} else {
				log.error("Failed to get initial totalCount from Onbid API. HTTP Status: {}",
						initialResponseEntity.getStatusCode());
				isSyncing = false;
				return;
			}
		} catch (Exception e) {
			log.error("Error fetching initial totalCount from Onbid API: {}", e.getMessage(), e);
			isSyncing = false;
			return;
		}

		List<CompletableFuture<List<TenderResponseDTO>>> futures = IntStream.rangeClosed(1, totalPages)
				.<CompletableFuture<List<TenderResponseDTO>>>mapToObj((int pageNum) -> {
					return CompletableFuture.<List<TenderResponseDTO>>supplyAsync(
							() -> fetchOnbidDataForSinglePage(pageNum), (Executor) onbidApiExecutor);
				}).collect(Collectors.toList());

		List<TenderResponseDTO> allUniqueTendersFromApi = new ArrayList<>();
		Set<String> processedCltrMnmtNos = new HashSet<>();

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		for (CompletableFuture<List<TenderResponseDTO>> future : futures) {
			try {
				List<TenderResponseDTO> pageTenders = future.get();
				for (TenderResponseDTO dto : pageTenders) {
					if (dto.getCltrMnmtNo() != null && !processedCltrMnmtNos.contains(dto.getCltrMnmtNo())) {
						allUniqueTendersFromApi.add(dto);
						processedCltrMnmtNos.add(dto.getCltrMnmtNo());
					}
				}
			} catch (Exception e) {
				log.error("Error getting result from CompletableFuture: {}", e.getMessage(), e);
			}
		}

		log.info("Finished fetching all {} unique tenders from Onbid API (parallel). Starting DB sync.",
				allUniqueTendersFromApi.size());

		transactionTemplate.execute(status -> {
			return saveOrUpdateTenders(allUniqueTendersFromApi);
		});

		long fullSyncEndTime = System.currentTimeMillis();
		log.info("Full Onbid Tender synchronization finished in {}ms (parallel).",
				(fullSyncEndTime - fullSyncStartTime));
		isSyncing = false;
	}

	private List<TenderResponseDTO> fetchOnbidDataForSinglePage(int page) {
		log.info(">>>> Started fetching page {} at {}", page, LocalDateTime.now());
		
		List<TenderResponseDTO> pageTenders = new ArrayList<>();
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(onbidApiBaseUrl)
				.queryParam("serviceKey", onbidApiServiceKey).queryParam("pageNo", page)
				.queryParam("numOfRows", 10000);

		URI uri = uriBuilder.encode().build().toUri();
		log.debug("Fetching Onbid API data for single page {}: {}", page, uri);

		try {
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);
			if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
				OnbidApiParser.TenderListResult parsedResult = onbidApiParser
						.parseXmlToTenderDtosAndCount(responseEntity.getBody());
				pageTenders.addAll(parsedResult.getTenders());
			} else {
				log.error("Failed to fetch Onbid API data from page {}. HTTP Status: {}", page,
						responseEntity.getStatusCode());
			}
		} catch (Exception e) {
			log.error("Error fetching Onbid API data from page {}: {}", page, e.getMessage(), e);
		}
		log.info("<<<< Finished fetching page {} at {}", page, LocalDateTime.now());
		
		return pageTenders;		
	}

	private List<TenderResponseDTO> fetchOnbidDataPages(int startPage, int endPage) {
		List<TenderResponseDTO> collectedTenders = new ArrayList<>();
		Set<String> cltrMnmtNosInBatch = new HashSet<>();

		for (int page = startPage; page <= endPage; page++) {
			UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(onbidApiBaseUrl)
					.queryParam("serviceKey", onbidApiServiceKey).queryParam("pageNo", page)
					.queryParam("numOfRows", MAX_ONBID_API_NUM_OF_ROWS);

			URI uri = uriBuilder.encode().build().toUri();
			log.info("Fetching Onbid API data from page {}/{} ({} rows): {}", page, endPage, MAX_ONBID_API_NUM_OF_ROWS,
					uri);

			try {
				ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

				if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
					OnbidApiParser.TenderListResult parsedResult = onbidApiParser
							.parseXmlToTenderDtosAndCount(responseEntity.getBody());
					List<TenderResponseDTO> dtoList = parsedResult.getTenders();

					for (TenderResponseDTO dto : dtoList) {
						if (dto.getCltrMnmtNo() != null && !cltrMnmtNosInBatch.contains(dto.getCltrMnmtNo())) {
							collectedTenders.add(dto);
							cltrMnmtNosInBatch.add(dto.getCltrMnmtNo());
						}
					}
				} else {
					log.error("Failed to fetch Onbid API data from page {}. HTTP Status: {}", page,
							responseEntity.getStatusCode());
				}
			} catch (Exception e) {
				log.error("Error fetching Onbid API data from page {}: {}", page, e.getMessage(), e);
			}
		}
		return collectedTenders;
	}

	private int saveOrUpdateTenders(List<TenderResponseDTO> apiTenders) {
		AtomicInteger newCount = new AtomicInteger(0);
		AtomicInteger updatedCount = new AtomicInteger(0);

		Set<String> cltrMnmtNosFromApi = apiTenders.stream().map(TenderResponseDTO::getCltrMnmtNo)
				.filter(Objects::nonNull).collect(Collectors.toSet());

		// 기존 DB 데이터와 비교하여 비활성화할 대상을 찾기 위함
		List<Tender> existingAllTendersInDb = tenderRepository.findAll();
		Set<String> existingCltrMnmtNosInDb = existingAllTendersInDb.stream().map(Tender::getCltrMnmtNo)
				.filter(Objects::nonNull).collect(Collectors.toSet());

		// 삽입/업데이트
		for (TenderResponseDTO dto : apiTenders) {
			if (dto.getCltrMnmtNo() == null || dto.getCltrMnmtNo().isEmpty()) {
				log.warn("Skipping tender with null or empty cltrMnmtNo from API: {}", dto.getTenderTitle());
				continue;
			}

			Optional<Tender> existingTenderOpt = tenderRepository.findByCltrMnmtNo(dto.getCltrMnmtNo());

			if (existingTenderOpt.isPresent()) {
				Tender existingTender = existingTenderOpt.get();
				// 모든 필드 업데이트 (필요에 따라 더 세분화 가능)
				existingTender.setTenderId(dto.getTenderId());
				existingTender.setPbctNo(dto.getPbctNo());
				existingTender.setCltrHstrNo(dto.getCltrHstrNo());
				existingTender.setTenderTitle(dto.getTenderTitle());
				existingTender.setOrganization(dto.getOrganization());
				existingTender.setBidNumber(dto.getBidNumber());
				existingTender.setGoodsName(dto.getGoodsName());
				existingTender.setAnnouncementDate(dto.getAnnouncementDate());
				existingTender.setDeadline(dto.getDeadline());
				existingTender.setLastSyncedAt(LocalDateTime.now());
				existingTender.setActive(true); // API에서 조회되었으므로 활성 상태

				tenderRepository.save(existingTender);
				updatedCount.incrementAndGet();
			} else {
				Tender newTender = Tender.fromDto(dto);
				tenderRepository.save(newTender);
				newCount.incrementAndGet();
			}
		}

		// 비활성화 처리: API에서 더 이상 조회되지 않지만 DB에는 있는 항목
		AtomicInteger deactivatedCount = new AtomicInteger(0);
		Set<String> cltrMnmtNosToDeactivate = existingCltrMnmtNosInDb.stream()
				.filter(mnmtNo -> !cltrMnmtNosFromApi.contains(mnmtNo)).collect(Collectors.toSet());

		if (!cltrMnmtNosToDeactivate.isEmpty()) {
			List<Tender> tendersToDeactivate = existingAllTendersInDb.stream()
					.filter(t -> cltrMnmtNosToDeactivate.contains(t.getCltrMnmtNo())).collect(Collectors.toList());
			tendersToDeactivate.forEach(t -> {
				if (t.isActive()) { // 이미 비활성화된 것은 스킵
					t.setActive(false);
					deactivatedCount.incrementAndGet();
				}
			});
			tenderRepository.saveAll(tendersToDeactivate);
		}

		log.info("DB sync summary - New: {}, Updated: {}, Deactivated: {}", newCount.get(), updatedCount.get(),
				deactivatedCount.get());
		return apiTenders.size(); // 동기화 처리된 API 항목 수 반환 (주요 지표는 아님)
	}
}
