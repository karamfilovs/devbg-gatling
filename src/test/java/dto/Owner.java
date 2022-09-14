package dto;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Builder;

@Builder
public class Owner {
    private String address;
    private String city;
    private String firstName;
    private String lastName;
    private String telephone;
    private String id;

    public static void main(String[] args) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Owner newOwner = Owner.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .build();
        System.out.println(gson.toJson(newOwner));
    }
}

