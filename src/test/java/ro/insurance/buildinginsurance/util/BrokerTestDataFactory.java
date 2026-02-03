package ro.insurance.buildinginsurance.util;

import ro.insurance.buildinginsurance.domain.entity.Broker;

public class BrokerTestDataFactory {

    private BrokerTestDataFactory() {}

    // -------------------  ENTITIES  -------------------

    public static Broker aBrokerWithId(Long id) {
        Broker broker = new Broker();
        broker.setId(id);
        broker.setActive(true);
        broker.setName("Test Broker");
        return broker;
    }

    public static Broker anInactiveBrokerWithId(Long id) {
        Broker b = aBrokerWithId(id);
        b.setActive(false);
        return b;
    }

}
