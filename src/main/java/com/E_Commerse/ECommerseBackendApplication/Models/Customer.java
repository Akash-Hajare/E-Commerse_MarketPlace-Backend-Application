package com.E_Commerse.ECommerseBackendApplication.Models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="customer")
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private int age;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String mobNo;

    @OneToMany(mappedBy = "customer",cascade = CascadeType.ALL)
    private List<Card> cards = new ArrayList<>();

    @OneToOne(mappedBy = "customer",cascade = CascadeType.ALL)
    private Cart cart;

    @OneToMany(mappedBy = "customer",cascade = CascadeType.ALL)
    private List<Ordered> orders = new ArrayList<>();

}
