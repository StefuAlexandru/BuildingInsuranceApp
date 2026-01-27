package ro.insurance.buildinginsurance.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.application.mapper.CurrencyMapper;
import ro.insurance.buildinginsurance.application.service.ICurrencyService;
import ro.insurance.buildinginsurance.domain.entity.Currency;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.CurrencyRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.PolicyRepository;
import ro.insurance.buildinginsurance.web.dto.currencyDto.CurrencyRequestDTO;
import ro.insurance.buildinginsurance.web.dto.currencyDto.CurrencyResponseDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import static ro.insurance.buildinginsurance.util.StringNormalizer.normalizeUpper;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CurrencyService implements ICurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;
    private final PolicyRepository policyRepository;

    // --------------------- CREATE ---------------------

    @Override
    public CurrencyResponseDTO registerCurrency(CurrencyRequestDTO currencyRequestDTO) {
        log.info("Currency register request: name={}, code={}", currencyRequestDTO.name(), currencyRequestDTO.code());
        validateCreateOrThrow(currencyRequestDTO);
        Currency currency = currencyMapper.toEntity(currencyRequestDTO);
        Currency saved = currencyRepository.save(currency);
        return currencyMapper.toResponse(saved);
    }

    // --------------------- GET ---------------------

    @Override
    @Transactional(readOnly = true)
    public Page<CurrencyResponseDTO> getAllCurrencies(Pageable pageable) {
        log.info("Get all currencies: page={}, size={}, sort={}", pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return currencyRepository.findAll(pageable)
                .map(currencyMapper::toResponse);
    }

    // --------------------- UPDATE ---------------------

    @Override
    public CurrencyResponseDTO activate(Long currencyId) {
        log.info("Activate currency: id={}", currencyId);
        Currency currency = findCurrencyOrThrow(currencyId);
        log.info("Activating currency: id={}, code={}", currencyId, currency.getCode());

        if(currency.isActive()) {
            return currencyMapper.toResponse(currency);
        }
        currency.setActive(true);


        Currency saved = currencyRepository.save(currency);
        return currencyMapper.toResponse(saved);
    }

    @Override
    public CurrencyResponseDTO deactivate(Long currencyId) {
        log.info("Deactivate request: id={}", currencyId);

        Currency currency = findCurrencyOrThrow(currencyId);
        if(!currency.isActive()) {
            return currencyMapper.toResponse(currency);
        }

        validateCurrencyDeactivation(currency);

        log.info("Deactivating currency: id={}, code={}", currencyId, currency.getCode());
        currency.setActive(false);

        Currency saved = currencyRepository.save(currency);
        return currencyMapper.toResponse(saved);
    }

    // --------------------- HELPERS ---------------------

    private void validateCreateOrThrow(CurrencyRequestDTO currencyRequestDTO) {
        String code = normalizeUpper(currencyRequestDTO.code());
        if(currencyRepository.existsByCodeIgnoreCase(code)){
            log.warn("Currency code already exists: code={}", code);
            throw new BusinessException(ErrorCode.CURRENCY_CODE_ALREADY_EXISTS);
        }
    }

    private Currency findCurrencyOrThrow(Long currencyId) {
        return currencyRepository.findById(currencyId)
                .orElseThrow(() -> {
                    log.warn("Currency not found: id={}", currencyId);
                    return new BusinessException(ErrorCode.CURRENCY_NOT_FOUND);
                });
    }

    private void validateCurrencyDeactivation(Currency currency) {
        // check if exists any active policy for this currency
        if(policyRepository.existsActivePolicyByCurrencyCode(currency.getCode())){
            log.warn("Currency in use by active policy: code={}", currency.getCode());
            throw new BusinessException(ErrorCode.CURRENCY_IN_USE_BY_ACTIVE_POLICIES);
        }
    }

}
