package pt.psoft.g1.psoftg1.bookmanagement.services;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleBookItem {

    private GoogleVolumeInfo volumeInfo;

    public GoogleVolumeInfo getVolumeInfo() {
        return volumeInfo;
    }

    public void setVolumeInfo(GoogleVolumeInfo volumeInfo) {
        this.volumeInfo = volumeInfo;
    }


}
