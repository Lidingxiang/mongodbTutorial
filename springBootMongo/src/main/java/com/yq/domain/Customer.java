
package com.yq.domain;

/**
 * Simple to Introduction
 * className: Customer
 *
 * @author EricYang
 * @version 2018/5/13 19:14
 */
import org.springframework.data.annotation.Id;


public class Customer {

    @Id
    public String id;

    public String firstName;
    public String lastName;

    public Customer() {}

    public Customer(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return String.format(
                "Customer[id=%s, firstName='%s', lastName='%s']",
                id, firstName, lastName);
    }

    public String getId() {
        return id;
    }

}
