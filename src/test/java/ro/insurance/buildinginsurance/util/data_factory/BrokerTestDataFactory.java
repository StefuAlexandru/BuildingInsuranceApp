package ro.insurance.buildinginsurance.util.data_factory;

import ro.insurance.buildinginsurance.domain.entity.Broker;
import ro.insurance.buildinginsurance.web.dto.broker_dto.BrokerRequestDTO;

import java.math.BigDecimal;

public class BrokerTestDataFactory {

    private BrokerTestDataFactory() {}

    // -------------------  DTOS  -------------------

    public static BrokerRequestDTO aBrokerRequest(){
        return new BrokerRequestDTO(
                "CODE-111","Test Broker","broker@email.com",
                "0777777777",new BigDecimal("10")
        );
    }

    // -------------------  ENTITIES  -------------------

    public static Broker aBrokerWithId(Long id) {
        Broker broker = new Broker();
        broker.setId(id);
        broker.setActive(true);
        broker.setBrokerCode("CODE-111");
        broker.setEmail("broker@email.com");
        broker.setPhone("0777777777");
        broker.setName("Test Broker");
        broker.setCommissionPct(new BigDecimal("10"));
        return broker;
    }

    public static Broker anInactiveBrokerWithId(Long id) {
        Broker b = aBrokerWithId(id);
        b.setActive(false);
        return b;
    }

}
