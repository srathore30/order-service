package sfa.order_service.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.constant.Status;
import sfa.order_service.dto.request.SampleReq;
import sfa.order_service.dto.response.PaginatedResp;
import sfa.order_service.dto.response.SampleInventoryResponse;
import sfa.order_service.dto.response.SampleRes;
import sfa.order_service.entity.SamplesEntity;
import sfa.order_service.exception.NoSuchElementFoundException;
import sfa.order_service.repo.SamplesRepo;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SampleServices {
    private final SamplesRepo samplesRepo;
    private final ProductServiceClient productServiceClient;
    private final ExternalRestService externalRestService;

    @Transactional
    public SampleRes createSample(SampleReq sampleReq){
        SamplesEntity samplesEntity = mapToEntity(sampleReq);
        SampleInventoryResponse sampleInventoryResponse = externalRestService.getSampleInventory(sampleReq.getMemberId(), sampleReq.getProductId());
        if(sampleInventoryResponse.getSampleQuantity() < samplesEntity.getQuantity()){
            throw new NoSuchElementFoundException(ApiErrorCodes.INSUFFICIENT_QUANTITY.getErrorCode(), ApiErrorCodes.INSUFFICIENT_QUANTITY.getErrorMessage());
        }
        externalRestService.deductSampleInventory(sampleInventoryResponse.getId(), sampleInventoryResponse.getSampleQuantity() - samplesEntity.getQuantity());
        return mapToDto(samplesRepo.save(samplesEntity));
    }
    @Transactional
    public SampleRes updateSample(Long id, SampleReq sampleReq){
        Optional<SamplesEntity> optionalSamplesEntity = samplesRepo.findById(id);
        if(optionalSamplesEntity.isEmpty()){
            throw new NoSuchElementFoundException(ApiErrorCodes.SAMPLE_NOT_FOUND.getErrorCode(), ApiErrorCodes.SAMPLE_NOT_FOUND.getErrorMessage());
        }
        if(Objects.equals(sampleReq.getQuantity(), optionalSamplesEntity.get().getQuantity())){
            updateEntityFromDto(optionalSamplesEntity.get(), sampleReq);
            return mapToDto(samplesRepo.save(optionalSamplesEntity.get()));
        }
        updateEntityFromDto(optionalSamplesEntity.get(), sampleReq);
        SampleInventoryResponse sampleInventoryResponse = externalRestService.getSampleInventory(sampleReq.getMemberId(), sampleReq.getProductId());
        if(sampleInventoryResponse.getSampleQuantity() < sampleReq.getQuantity()){
            throw new NoSuchElementFoundException(ApiErrorCodes.INSUFFICIENT_QUANTITY.getErrorCode(), ApiErrorCodes.INSUFFICIENT_QUANTITY.getErrorMessage());
        }
        if(sampleReq.getQuantity() > optionalSamplesEntity.get().getQuantity()){
            Integer newQuantity = sampleInventoryResponse.getSampleQuantity() - (sampleReq.getQuantity() - optionalSamplesEntity.get().getQuantity());
            externalRestService.deductSampleInventory(sampleInventoryResponse.getId(), newQuantity);
        } else{
            Integer newQuantity = sampleInventoryResponse.getSampleQuantity() + (optionalSamplesEntity.get().getQuantity() - sampleReq.getQuantity());
            externalRestService.deductSampleInventory(sampleInventoryResponse.getId(), newQuantity);
        }
        return mapToDto(samplesRepo.save(optionalSamplesEntity.get()));
    }

    public SampleRes getSampleById(Long sampleId){
        Optional<SamplesEntity> optionalSamplesEntity = samplesRepo.findById(sampleId);
        if(optionalSamplesEntity.isEmpty()){
            throw new NoSuchElementFoundException(ApiErrorCodes.SAMPLE_NOT_FOUND.getErrorCode(), ApiErrorCodes.SAMPLE_NOT_FOUND.getErrorMessage());
        }
        return mapToDto(optionalSamplesEntity.get());
    }

    public void deleteSample(Long sampleId){
        Optional<SamplesEntity> optionalSamplesEntity = samplesRepo.findById(sampleId);
        if(optionalSamplesEntity.isEmpty()){
            throw new NoSuchElementFoundException(ApiErrorCodes.SAMPLE_NOT_FOUND.getErrorCode(), ApiErrorCodes.SAMPLE_NOT_FOUND.getErrorMessage());
        }
        optionalSamplesEntity.get().setStatus(Status.Inactive);
        samplesRepo.save(optionalSamplesEntity.get());
    }

    @Transactional
    public List<SampleRes> createSampleInBulk(List<SampleReq> sampleReqList){
        List<SampleRes> sampleResList = new ArrayList<>();
        for(SampleReq sampleReq : sampleReqList) {
            SamplesEntity samplesEntity = mapToEntity(sampleReq);
            SampleInventoryResponse sampleInventoryResponse = externalRestService.getSampleInventory(sampleReq.getMemberId(), sampleReq.getProductId());
            if (sampleInventoryResponse.getSampleQuantity() < samplesEntity.getQuantity()) {
                throw new NoSuchElementFoundException(ApiErrorCodes.INSUFFICIENT_QUANTITY.getErrorCode(), ApiErrorCodes.INSUFFICIENT_QUANTITY.getErrorMessage());
            }
            externalRestService.deductSampleInventory(sampleInventoryResponse.getId(), sampleInventoryResponse.getSampleQuantity() - samplesEntity.getQuantity());
            sampleResList.add(mapToDto(samplesRepo.save(samplesEntity)));
        }
        return sampleResList;
    }
    public PaginatedResp<SampleRes> getAllSampleByReportingManagerMembers(Long memberId, int page, int pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        log.info("inside of getAllSampleByReportingManagerMembers");
        Set<Long> memberIds = productServiceClient.getAllMemberIdsByReportingManager(memberId);
        Page<SamplesEntity> samplesEntityPage = samplesRepo.findByMembersIdList(memberIds, pageable);
        List<SampleRes> sampleResList = samplesEntityPage.stream().map(this::mapToDto).toList();
        return new PaginatedResp<>(samplesEntityPage.getTotalElements(), samplesEntityPage.getTotalPages(), page, sampleResList);
    }

    public PaginatedResp<SampleRes> getAllSampleByMemberId(Long memberId, int page, int pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        log.info("inside of getAllSampleByMemberId");
        Page<SamplesEntity> samplesEntityPage = samplesRepo.findByMemberId(memberId, pageable);
        List<SampleRes> sampleResList = samplesEntityPage.stream().filter(samplesEntity -> samplesEntity.getStatus() != Status.Inactive).map(this::mapToDto).toList();
        return new PaginatedResp<>(samplesEntityPage.getTotalElements(), samplesEntityPage.getTotalPages(), page, sampleResList);
    }

    public PaginatedResp<SampleRes> getAllSamples(int page, int pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        log.info("inside of getAllSamples");
        Page<SamplesEntity> samplesEntityPage = samplesRepo.findAll(pageable);
        List<SampleRes> sampleResList = samplesEntityPage.stream().map(this::mapToDto).toList();
        return new PaginatedResp<>(samplesEntityPage.getTotalElements(), samplesEntityPage.getTotalPages(), page, sampleResList);
    }

    private SampleRes mapToDto(SamplesEntity sample){
        SampleRes sampleRes = new SampleRes();
        sampleRes.setId(sample.getId());
        sampleRes.setBeetLogId(sample.getBeetLogId());
        sampleRes.setClientLogId(sample.getClientLogId());
        sampleRes.setDoctorLogId(sample.getDoctorLogId());
        sampleRes.setBundleType(sample.getBundleType());
        sampleRes.setSampleDate(sample.getSampleDate());
        sampleRes.setProductRes(productServiceClient.getProduct(sample.getProductId()));
        sampleRes.setQuantity(sample.getQuantity());
        sampleRes.setMemberResponse(externalRestService.getMember(sample.getMemberId()));
        if(sample.getDoctorId() != null){
            sampleRes.setDoctorRes(externalRestService.getDoctor(sample.getDoctorId()));
        } if (sample.getClientFmcgId() != null) {
            sampleRes.setClientFMCGResponse(externalRestService.getClient(sample.getClientFmcgId()));
        } if(sample.getOutletId() != null){
            sampleRes.setOutletRespForOrderDto(externalRestService.getOutletByIdWithResp(sample.getOutletId()));
        }
        return sampleRes;
    }

    private SamplesEntity mapToEntity(SampleReq sampleReq){
        SamplesEntity samplesEntity = new SamplesEntity();
        samplesEntity.setQuantity(sampleReq.getQuantity());
        samplesEntity.setStatus(Status.Active);
        samplesEntity.setBeetLogId(sampleReq.getBeetLogId());
        samplesEntity.setClientLogId(sampleReq.getClientLogId());
        samplesEntity.setDoctorLogId(sampleReq.getDoctorLogId());
        samplesEntity.setSampleDate(new Date());
        samplesEntity.setClientFmcgId(sampleReq.getClientFmcgId());
        samplesEntity.setOutletId(sampleReq.getOutletId());
        samplesEntity.setBundleType(sampleReq.getBundleType());
        samplesEntity.setDoctorId(sampleReq.getDoctorId());
        samplesEntity.setProductId(sampleReq.getProductId());
        samplesEntity.setMemberId(sampleReq.getMemberId());
        return samplesEntity;
    }

    private void updateEntityFromDto(SamplesEntity samplesEntity, SampleReq sampleReq){
        samplesEntity.setDoctorId(sampleReq.getDoctorId());
        if(sampleReq.getBundleType() != null){
            samplesEntity.setBundleType(sampleReq.getBundleType());
        }
        samplesEntity.setProductId(sampleReq.getProductId());
        samplesEntity.setMemberId(sampleReq.getMemberId());
        samplesEntity.setBundleType(sampleReq.getBundleType());
    }

}
