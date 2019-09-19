package com.test

import com.vladmihalcea.hibernate.type.json.*
import groovy.transform.CompileStatic
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.search.annotations.Field
import org.hibernate.search.annotations.Indexed
import org.hibernate.search.annotations.TermVector
import org.locationtech.jts.geom.Point

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id



@CompileStatic
@Entity
@TypeDef(
        name = "json",
        typeClass = JsonStringType.class
)
@Indexed
class Publisher {
    @Id
    @GeneratedValue
    Long id

    @Field(termVector = TermVector.YES)
    String name

    @Type(type = "json")
    @Column(columnDefinition = "json")
    String book

    @Field
    Double longitude
    @Field
    Double lattitude

    Point location
}
