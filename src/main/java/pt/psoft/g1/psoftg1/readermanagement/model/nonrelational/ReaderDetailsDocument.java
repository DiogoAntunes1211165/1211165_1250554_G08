package pt.psoft.g1.psoftg1.readermanagement.model.nonrelational;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.genremanagement.model.nonrelational.GenreDocument;
import pt.psoft.g1.psoftg1.shared.model.nonrelational.EntityWithPhotoDocument;
import pt.psoft.g1.psoftg1.usermanagement.model.nonrelational.ReaderDocument;

import java.nio.file.InvalidPathException;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "reader_details")
public class ReaderDetailsDocument extends EntityWithPhotoDocument {

    @Id
    private String id;

    // Referência ao documento de Reader
    @DBRef
    @Field("reader")
    private ReaderDocument reader;

    @Field("reader_number")
    private ReaderNumberDocument readerNumber;

    @Field("birth_date")
    private BirthDateDocument birthDate;

    @Field("phone_number")
    private PhoneNumberDocument phoneNumber;

    @Field("gdpr_consent")
    private boolean gdprConsent;

    @Field("marketing_consent")
    private boolean marketingConsent;

    @Field("third_party_sharing_consent")
    private boolean thirdPartySharingConsent;

    @Version
    private Long version;

    // Lista de géneros de interesse (referências)
    @DBRef
    @Field("interest_list")
    private List<GenreDocument> interestList;

    public ReaderDetailsDocument(
            int readerNumber,
            ReaderDocument reader,
            String birthDate,
            String phoneNumber,
            boolean gdpr,
            boolean marketing,
            boolean thirdParty,
            String photoURI,
            List<GenreDocument> interestList
    ) {
        if (reader == null || phoneNumber == null) {
            throw new IllegalArgumentException("Provided argument resolves to null object");
        }

        if (!gdpr) {
            throw new IllegalArgumentException("Readers must agree with the GDPR rules");
        }

        this.reader = reader;
        this.readerNumber = new ReaderNumberDocument(readerNumber);
        this.phoneNumber = new PhoneNumberDocument(phoneNumber);
        this.birthDate = new BirthDateDocument(birthDate);
        this.gdprConsent = true;
        setPhotoInternal(photoURI);
        this.marketingConsent = marketing;
        this.thirdPartySharingConsent = thirdParty;
        this.interestList = interestList;
    }

    public void applyPatch(final long currentVersion, final String birthDate, final String phoneNumber,
                           boolean marketing, boolean thirdParty, String photoURI, List<GenreDocument> interestList) {
        if (currentVersion != this.version) {
            throw new ConflictException("Provided version does not match latest version of this object");
        }

        if (birthDate != null) this.birthDate = new BirthDateDocument(birthDate);
        if (phoneNumber != null) this.phoneNumber = new PhoneNumberDocument(phoneNumber);
        this.marketingConsent = marketing;
        this.thirdPartySharingConsent = thirdParty;
        if (photoURI != null) {
            try {
                setPhotoInternal(photoURI);
            } catch (InvalidPathException ignored) {}
        }
        if (interestList != null) this.interestList = interestList;
    }

    public void removePhoto(long desiredVersion) {
        if (desiredVersion != this.version) {
            throw new ConflictException("Provided version does not match latest version of this object");
        }
        setPhotoInternal(null);
    }

    public String getReaderNumber() {
        return this.readerNumber.toString();
    }

    public String getPhoneNumber() {
        return this.phoneNumber.toString();
    }
}
