package ro.insurance.buildinginsurance.util;

import ro.insurance.buildinginsurance.domain.entity.Broker;

public class BrokerTestDataFactory {
    public static Broker aBrokerWithId(Long id) {
        Broker broker = new Broker();
        broker.setId(id);
        broker.setName("Test Broker");
        return broker;
    }
}
