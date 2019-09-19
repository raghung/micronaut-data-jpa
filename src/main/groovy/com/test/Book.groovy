package com.test

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id
    String title
    int pages
}
