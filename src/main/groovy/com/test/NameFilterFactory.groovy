package com.test

import groovy.transform.CompileStatic
import org.apache.lucene.index.Term
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.hibernate.search.annotations.Factory

@CompileStatic
class NameFilterFactory {

    String name

    @Factory
    Query getName() {
        new TermQuery(new Term("name", name))
    }
}
