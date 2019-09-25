package com.test

import groovy.transform.ToString
import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import org.hibernate.search.annotations.DateBridge
import org.hibernate.search.annotations.Resolution

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.TableGenerator
import java.time.LocalDate
import java.time.LocalDateTime

@ToString
@Entity
class Book {
    @Id
    @TableGenerator(name = "BOOK_SEQ")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "BOOK_SEQ")
    Long id
    String title
    int pages

    @DateBridge(resolution = Resolution.DAY)
    LocalDate publishDate

    @DateCreated
    LocalDateTime createDate
    @DateUpdated
    LocalDateTime updateDate

    Map display() {
        ["id": id,
         "title": title,
         "pages": pages,
         "publishDate": publishDate.toString(),
         "createDate": createDate.toString(),
         "updateDate": updateDate.toString()]
    }

}
