package dto;


import lombok.Builder;

@Builder
public class Owner {
    private String address;
    private String city;
    private String firstName;
    private String lastName;
    private String telephone;
    private String id;
}
