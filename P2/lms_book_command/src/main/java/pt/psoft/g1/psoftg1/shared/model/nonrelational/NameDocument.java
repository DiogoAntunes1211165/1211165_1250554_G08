package pt.psoft.g1.psoftg1.shared.model.nonrelational;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import pt.psoft.g1.psoftg1.shared.model.StringUtilsCustom;

@Getter
@Document(collection = "names")
public class NameDocument {
    @NotNull
    @NotBlank
    @Field("name")
    private String name;

    public NameDocument(String name){
        setName(name);
    }

    public void setName(String name){
        if(name == null)
            throw new IllegalArgumentException("Name cannot be null");
        if(name.isBlank())
            throw new IllegalArgumentException("Name cannot be blank, nor only white spaces");
        if(!StringUtilsCustom.isAlphanumeric(name))
            throw new IllegalArgumentException("Name can only contain alphanumeric characters");

        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    protected NameDocument() {}
}
