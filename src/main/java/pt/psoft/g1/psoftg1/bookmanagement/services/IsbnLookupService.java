package pt.psoft.g1.psoftg1.bookmanagement.services;

import java.util.Optional;

public interface IsbnLookupService {

    Optional<String> findIsbnByTitle(String title);

    String getServiceName();


}
