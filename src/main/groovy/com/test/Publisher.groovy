package com.test

import com.vladmihalcea.hibernate.type.json.*
import groovy.transform.CompileStatic
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.search.annotations.Analyze
import org.hibernate.search.annotations.Field
import org.hibernate.search.annotations.FullTextFilterDef
import org.hibernate.search.annotations.Index
import org.hibernate.search.annotations.Indexed
import org.hibernate.search.annotations.Latitude
import org.hibernate.search.annotations.Longitude
import org.hibernate.search.annotations.SortableField
import org.hibernate.search.annotations.Spatial
import org.hibernate.search.annotations.SpatialMode
import org.hibernate.search.annotations.Store
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
@Spatial(spatialMode = SpatialMode.HASH)
@FullTextFilterDef(name = "name", impl = NameFilterFactory)
class Publisher {
    @Id
    @GeneratedValue
    Long id

    //@Field(termVector = TermVector.NO)
    @Field(index = Index.YES, analyze = Analyze.NO, store = Store.YES, termVector = TermVector.NO)
    @SortableField  // For hibernate-search
    String name

    @Type(type = "json")
    @Column(columnDefinition = "json")
    String book

    // This is for hibernate-search library
    @Longitude
    Double longitude
    @Latitude
    Double latitude

    @Longitude(of = "home")
    Double homeLongitude
    @Latitude(of = "home")
    Double homeLatitude

    // This is for hibernate-spatial library
    Point location

    Publisher(String name, String book, Double longitude, Double latitude) {
        this.name = name
        this.book = book
        this.longitude = longitude
        this.latitude = latitude
    }

    Publisher() {}
}
