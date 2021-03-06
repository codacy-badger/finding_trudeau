package io.github.makbn.resources;

import com.codahale.metrics.annotation.Timed;
import io.github.makbn.api.common.Response;
import io.github.makbn.api.post.PostType;
import io.github.makbn.api.post.Posts;
import io.github.makbn.core.ApplicationSettings;
import io.github.makbn.core.exception.ApplicationException;
import io.github.makbn.core.exception.InvalidRequestException;
import io.github.makbn.core.service.crawler.CrawlerService;
import io.github.makbn.core.utils.DateUtils;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

/**
 * by Mehdi Akbarian Rastaghi , 20/4/10
 **/

@Path("/post")
@Produces(MediaType.APPLICATION_JSON)
public class PostsResource {
    @Inject
    private CrawlerService crawlerService;

    private static void checkLimitation(Integer limitation) {
        if (limitation < 1 || limitation > 100)
            throw InvalidRequestException.builder()
                    .message("limitation number is not valid!")
                    .build();
    }

    private static Date mapDate(String date) {
        try {
            return DateUtils.convert(date);
        } catch (ParseException e) {
            throw InvalidRequestException.builder()
                    .message("from date format is invalid!")
                    .build();
        }
    }

    /**
     * @param type filter by type, valid values are
     * @return all posts from both providers-twitter and CNN
     */
    @GET
    @Timed
    public Response<Posts> getAllPosts(@QueryParam("type") Optional<String> type,
                                       @QueryParam("from") Optional<String> from,
                                       @QueryParam("to") Optional<String> to,
                                       @QueryParam("limitation") Optional<Integer> limitation) throws ApplicationException {

        limitation.ifPresent(PostsResource::checkLimitation);
        type.ifPresent(PostType::checkTypeName);

        Optional<Date> fromDate = from.map(PostsResource::mapDate);
        Optional<Date> toDate = from.map(PostsResource::mapDate);

        Posts posts = crawlerService.getPosts(fromDate.orElse(DateUtils.getPreviousYear()),
                toDate.orElse(DateUtils.getCurrentDate()),
                limitation.orElse(ApplicationSettings.getDefaultLimitationNumber()),
                PostType.valueOf(type.orElse("ALL")));

        return Response.<Posts>builder()
                .result(posts)
                .build();
    }
}
