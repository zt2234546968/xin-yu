package com.xinyu.ecommerce.service;

import com.xinyu.ecommerce.entity.Ceping;
import java.util.List;

public interface CepingService {

    List<Ceping> getAllCepings();

    Ceping getCepingById(String id);

    Ceping createCeping(Ceping ceping);

    Ceping updateCeping(String id, Ceping ceping);

    void deleteCeping(String id);

    String generateCode();
}
