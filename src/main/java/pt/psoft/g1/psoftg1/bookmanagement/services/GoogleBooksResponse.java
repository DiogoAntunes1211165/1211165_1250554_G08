package pt.psoft.g1.psoftg1.bookmanagement.services;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleBooksResponse {

    private List<GoogleBookItem> items;

    public List<GoogleBookItem> getItems() {
        return items;
    }


}
