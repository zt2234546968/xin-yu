package com.xinyu.ecommerce.generator;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

public class CustomIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        String query = "SELECT MAX(z.id) FROM zhiping z";
        Integer maxId = (Integer) session.createNativeQuery(query).getSingleResult();
        if (maxId == null) {
            return 1000;
        }
        return maxId + 1;
    }
}
