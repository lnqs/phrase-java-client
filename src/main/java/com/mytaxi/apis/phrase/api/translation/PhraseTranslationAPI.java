package com.mytaxi.apis.phrase.api.translation;

import com.google.common.base.Preconditions;
import com.mytaxi.apis.phrase.api.GenericPhraseAPI;
import com.mytaxi.apis.phrase.api.translation.dto.PhraseTranslationDTO;
import com.mytaxi.apis.phrase.domainobject.translation.PhraseTranslation;
import com.mytaxi.apis.phrase.exception.PhraseAppApiException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Downloads the translations from phraseApp as POJOs.
 */
public class PhraseTranslationAPI extends GenericPhraseAPI<PhraseTranslationDTO[]>
{
    private static final Logger LOG = LoggerFactory.getLogger(PhraseTranslationAPI.class);

    // ---- configuration -----
    private static final String PLACEHOLDER_PROJECT_ID = "{projectid}";

    private static final String PLACEHOLDER_LOCALE_ID = "{localeid}";

    private static final String PHRASE_TRANSLATIONS_PATH = "/api/v2/projects/{projectid}/locales/{localeid}/translations";


    PhraseTranslationAPI(final RestTemplate restTemplate, final String authToken)
    {
        super(restTemplate, authToken);
    }


    PhraseTranslationAPI(final String authToken)
    {
        super(createRestTemplateWithConverter(), authToken);
    }


    /**
     * Retrieves all translations for the given projectId and the localeId.
     *
     * @param projectId - id of the PhraseApp project
     * @param localeId  - id of the specific locale
     * @return list of retrieved PhraseTranslations
     * @throws PhraseAppApiException - if some error occured due the whole process
     */
    public List<PhraseTranslation> listTranslations(String projectId, String localeId) throws PhraseAppApiException
    {
        Preconditions.checkNotNull(projectId, "ProjectId must not be null.");
        Preconditions.checkNotNull(localeId, "LocaleId must not be null.");
        LOG.trace("Start to retrieve translations for projectId: {} and localeId: {}", projectId, localeId);

        PhraseTranslationDTO[] requestedTranslations = null;
        try
        {
            String requestPath = createRequestPath(projectId, localeId);

            LOG.trace("Call requestPath: {} to get locales from phrase.", requestPath);

            final URIBuilder builder = createUriBuilder(requestPath);

            HttpEntity<Object> requestEntity = createHttpEntity(requestPath);

            URI uri = builder.build();

            ResponseEntity<PhraseTranslationDTO[]> responseEntity = requestPhrase(requestEntity, uri, PhraseTranslationDTO[].class);

            requestedTranslations = handleResponse(projectId, requestPath, responseEntity);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException("Something goes wrong due building of the request URI", e);
        }

        LOG.trace("Successfully retrieved {} translations for projectId: {} and localeId: {}", requestedTranslations.length, projectId, localeId);
        return PhraseTranslationMapper.makePhraseTranslations(requestedTranslations);
    }


    private String createRequestPath(String projectId, String localeId)
    {
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put(PLACEHOLDER_PROJECT_ID, projectId);
        placeholders.put(PLACEHOLDER_LOCALE_ID, localeId);
        return createPath(PHRASE_TRANSLATIONS_PATH, placeholders);
    }

}
