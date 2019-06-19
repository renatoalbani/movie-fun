package org.superbiz.moviefun;

import jdk.nashorn.internal.runtime.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;
    private final PlatformTransactionManager albumsTransactionManager;
    private final PlatformTransactionManager moviesTransactionManager;
    private static Logger log = LoggerFactory.getLogger(HomeController.class);

    public HomeController(MoviesBean moviesBean, AlbumsBean albumsBean, MovieFixtures movieFixtures,
                          AlbumFixtures albumFixtures, PlatformTransactionManager albumsPlatformTransactionManager, PlatformTransactionManager moviesPlatformTransactionManager) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
        this.albumsTransactionManager = albumsPlatformTransactionManager;
        this.moviesTransactionManager = moviesPlatformTransactionManager;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {

        TransactionCallbackWithoutResult moviesCallback = new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {

                try {

                    for (Movie movie : movieFixtures.load()) {
                        moviesBean.addMovie(movie);
                    }

                } catch (Exception ex) {
                    status.setRollbackOnly();
                    log.error("Failed to persist new movies", ex);
                }
            }
        };

        TransactionCallbackWithoutResult albumsCallback = new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    for (Album album : albumFixtures.load()) {
                        albumsBean.addAlbum(album);

                    }
                }catch (Exception ex){
                    log.error("Failed to persist albums", ex);
                    status.setRollbackOnly();
                }
            }
        };

        TransactionTemplate albumsTransactionTemplate = new TransactionTemplate(albumsTransactionManager);
        TransactionTemplate moviesTransactionTemplate = new TransactionTemplate(moviesTransactionManager);

        moviesTransactionTemplate.execute(moviesCallback);
        albumsTransactionTemplate.execute(albumsCallback);

        model.put("movies", moviesBean.getMovies());
        model.put("albums", albumsBean.getAlbums());

        return "setup";
    }
}
