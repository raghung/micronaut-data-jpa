package com.test

import com.opencsv.CSVReader
import com.opencsv.bean.ColumnPositionMappingStrategy
import com.opencsv.bean.CsvToBean
import com.opencsv.bean.HeaderColumnNameMappingStrategy
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.multipart.StreamingFileUpload
import io.reactivex.Single
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Point
import org.locationtech.jts.io.ParseException
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.util.GeometricShapeFactory

import javax.annotation.Nullable
import javax.inject.Inject
import java.time.LocalDate

@CompileStatic
@Slf4j
@Controller("/books")
class BookController {

    @Inject
    BookRepository bookRepository

    @Inject
    InterfaceBookRepository iBookRepository

    @Inject
    PublisherRepository publisherRepository

    @Get("/{title}")
    Book bookByTitle(String title) {
        //bookRepository.findAllByTitle(title)
        iBookRepository.findByTitle(title)?.get()
    }

    @Post("/pageable")
    Page<Book> findAll(@Body Pageable pageable) {
       /* {
            "number": 1,
            "size": 2,
            "sort": {
                "orderBy": [
                    {
                        "property": "title",
                        "direction": "ASC",
                        "ignoreCase": false
                    }
                ]
            }
        } */
        bookRepository.findAll(pageable)
    }

    @Post("/list")
    Page<Book> list(Integer offset, Integer max, String sortField, String sortDirection) {
        Sort.Order.Direction direction = Sort.Order.Direction.ASC
        if (sortDirection.equalsIgnoreCase("desc")) {
            direction = Sort.Order.Direction.DESC
        }
        Sort.Order order = new Sort.Order(sortField, direction, false)
        Sort sort = Sort.of([order])
        Pageable pageable = Pageable.from(offset, max, sort)
        Page<Book> pbook = bookRepository.findAll(pageable)
        List lstBooks = []
        pbook.content?.each { book ->
            lstBooks += book.display()
        }

        return pbook
        //bookRepository.findAll().asList()
    }

    @Put("/")
    Book save(String title, int pages, String name, Double longitude, Double latitude) {
        Book book = iBookRepository.findByTitle(title)?.get()

        if (!book) {
            LocalDate localDate = LocalDate.of(2019, 9, 24)
            book = new Book(title: title, pages: pages, publishDate: localDate)
            bookRepository.save(book)
        }

        // Save Publisher
//        String bookInfo = JsonOutput.toJson(book).toString()
//
//        Point point = (Point)wktToGeometry("POINT (${latitude} ${longitude})")
//
//        publisherRepository.save(new Publisher(name: name, book: bookInfo,
//                                               longitude: longitude, latitude: latitude, location: point))

        return book
    }

    @Get("/publisher/{id}")
    Map publisherInfo(Long id) {
        Publisher publisher = publisherRepository.findById(id)?.get()
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map book = (Map)jsonSlurper.parseText(publisher.book)
        log.info("Book title -> " + book.get("title"))

        return ["name": publisher.name,
                "book": book,
                "location": publisher.location.toString()]
    }

    @Post("/publisher/search/")
    List searchPublisher(String searchText, @Nullable String[] fields,
                         Integer offset, Integer max, String sortField, String sortDirection) {
        publisherRepository.searchPublishers(searchText, fields, offset, max, sortField, sortDirection)
    }

    @Post("/publisher/geosearch/")
    List searchPublisher(Double longitude, Double latitude, Integer radius,
                         Integer offset, Integer max, String sortField, String sortDirection) {
        publisherRepository.searchWithinRadius(longitude, latitude, radius, offset, max, sortField, sortDirection)
    }

    @Post("/publisher/withinRadius")
    List withinRadius(Double latitude, Double longitude, Double radius) {
        publisherRepository.withinRadius(latitude, longitude, radius)
    }

    @Post("/publisher/withFilter")
    List withinRadius(String searchText, Double longitude, Double latitude, Integer radius,
                      Integer offset, Integer max, String sortField, String sortDirection) {
        publisherRepository.searchWithFilter(searchText, longitude, latitude, radius,
                                                                        offset, max, sortField, sortDirection)
    }

    @Post(value = "/bulkupload", consumes = MediaType.MULTIPART_FORM_DATA)
    Single<MutableHttpResponse> bulkUpload(StreamingFileUpload file) {
        // Check for file content type
//        if (file.contentType.get() != 'text/csv') {
//            throw new Exception("Invalid file format")
//        }
        File tempFile = File.createTempFile(file.getFilename(), "temp")
        org.reactivestreams.Publisher<Boolean> uploadPublisher = file.transferTo(tempFile)//.transferTo(tempFile)
        return Single.fromPublisher(uploadPublisher)
                     .map({success ->
            if (success) {
                HttpResponse.ok(batchProcessBookCSV(tempFile))
            } else {
                HttpResponse.status(HttpStatus.CONFLICT).body("Upload Failed")
            }
        })
    }

    private Geometry wktToGeometry(String wellKnownText) throws ParseException {
        new WKTReader().read(wellKnownText)
    }

    private Map batchProcessBookCSV(File csvFile) {
        CSVReader reader = CSVReader.newInstance(new FileReader(csvFile))
        HeaderColumnNameMappingStrategy<Book> beanStrategy = new HeaderColumnNameMappingStrategy<Book>()
        beanStrategy.setType(Book)

        CsvToBean<Book> csvToBean = new CsvToBean<Book>()
        csvToBean.setMappingStrategy(beanStrategy)
        csvToBean.setCsvReader(reader)
        int successRows = 0
        int errorRows = 0
        csvToBean.parse()?.each { book ->
            try {
                bookRepository.save(book)
                successRows += 1
            } catch(Exception e) {
                log.info(e.message + " - " + book.toString())
                errorRows += 1
            }

        }

        return ["total": errorRows + successRows,
                "success": successRows,
                "Error": errorRows]

    }

}