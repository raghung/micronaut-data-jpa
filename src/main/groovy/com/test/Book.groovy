package com.test

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.TableGenerator

@Entity
class Book {
    @Id
    @TableGenerator(name = "BOOK_SEQ")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "BOOK_SEQ")
    Long id
    String title
    int pages
}
