package io.github.stefanbratanov.jvm.openai;

import static io.github.stefanbratanov.jvm.openai.TestConstants.OPEN_AI_SPECIFICATION_URL;
import static org.assertj.core.api.Assertions.assertThat;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.Request.Method;
import com.atlassian.oai.validator.model.Response;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.model.SimpleResponse;
import com.atlassian.oai.validator.report.ValidationReport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

class OpenApiSpecificationValidationTest {

  private static OpenApiInteractionValidator validator;

  private final TestDataUtil testDataUtil = new TestDataUtil();
  private final ObjectMapper objectMapper = ObjectMapperSingleton.getInstance();

  @BeforeAll
  public static void setUp() {
    OpenAPI api = new OpenAPIV3Parser().read(OPEN_AI_SPECIFICATION_URL);
    validator = OpenApiInteractionValidator.createFor(api).build();
  }

  @RepeatedTest(50)
  void validateAudio() {
    SpeechRequest speechRequest = testDataUtil.randomSpeechRequest();

    Request request =
        createRequestWithBody(
            Method.POST, "/" + Endpoint.SPEECH.getPath(), serializeObject(speechRequest));

    validate(request);
    // can't validate multipart/form-data so won't validate other endpoints
  }

  @RepeatedTest(50)
  void validateChat() {
    CreateChatCompletionRequest createChatCompletionRequest =
        testDataUtil.randomCreateChatCompletionRequest();

    ChatCompletion chatCompletion = testDataUtil.randomChatCompletion();

    Request request =
        createRequestWithBody(
            Method.POST,
            "/" + Endpoint.CHAT.getPath(),
            serializeObject(createChatCompletionRequest));

    Response response = createResponseWithBody(serializeObject(chatCompletion));

    validate(request, response);
  }

  @RepeatedTest(50)
  void validateEmbeddings() {
    EmbeddingsRequest embeddingsRequest = testDataUtil.randomEmbeddingsRequest();

    Embeddings embeddings = testDataUtil.randomEmbeddings();

    Request request =
        createRequestWithBody(
            Method.POST, "/" + Endpoint.EMBEDDINCS.getPath(), serializeObject(embeddingsRequest));

    Response response = createResponseWithBody(serializeObject(embeddings));

    validate(request, response);
  }

  @RepeatedTest(50)
  void validateFineTuning() {
    CreateFineTuningJobRequest createFineTuningJobRequest =
        testDataUtil.randomCreateFineTuningJobRequest();

    FineTuningJob fineTuningJob = testDataUtil.randomFineTuningJob();

    Request request =
        createRequestWithBody(
            Method.POST,
            "/" + Endpoint.FINE_TUNING.getPath(),
            serializeObject(createFineTuningJobRequest));

    Response response = createResponseWithBody(serializeObject(fineTuningJob));

    validate(request, response);

    FineTuningClient.PaginatedFineTuningJobs paginatedFineTuningJobs =
        testDataUtil.randomPaginatedFineTuningJobs();

    Response listJobsResponse = createResponseWithBody(serializeObject(paginatedFineTuningJobs));

    validate("/" + Endpoint.FINE_TUNING.getPath(), Method.GET, listJobsResponse);

    FineTuningClient.PaginatedFineTuningEvents paginatedFineTuningEvents =
        testDataUtil.randomPaginatedFineTuningEvents();

    Response listEventsResponse =
        createResponseWithBody(serializeObject(paginatedFineTuningEvents));

    validate(
        "/" + Endpoint.FINE_TUNING.getPath() + "/{fine_tuning_job_id}/events",
        Method.GET,
        listEventsResponse,
        // https://github.com/openai/openai-openapi/pull/168
        "Object instance has properties which are not allowed by the schema: [\"has_more\"]");

    FineTuningClient.PaginatedFineTuningCheckpoints paginatedFineTuningCheckpoints =
        testDataUtil.randomPaginatedFineTuningCheckpoints();

    Response listCheckpointsResponse =
        createResponseWithBody(serializeObject(paginatedFineTuningCheckpoints));

    validate(
        "/" + Endpoint.FINE_TUNING.getPath() + "/{fine_tuning_job_id}/checkpoints",
        Method.GET,
        listCheckpointsResponse,
        "Object has missing required properties ([\"n_epochs\"]");
  }

  @RepeatedTest(50)
  void validateFiles() {
    File file = testDataUtil.randomFile();

    // manually add deprecated required field "status"
    Response response =
        createResponseWithBody(serializeObject(file, Map.of("status", "processed")));

    validate("/" + Endpoint.FILES.getPath() + "/{file_id}", Method.GET, response);
  }

  @RepeatedTest(50)
  void validateImages() {
    CreateImageRequest createImageRequest = testDataUtil.randomCreateImageRequest();

    Request request =
        createRequestWithBody(
            Method.POST,
            "/" + Endpoint.IMAGE_GENERATION.getPath(),
            serializeObject(createImageRequest));

    Images images = testDataUtil.randomImages();

    Response response = createResponseWithBody(serializeObject(images));

    validate(request, response);
    // can't validate multipart/form-data so won't validate other endpoints
  }

  @RepeatedTest(50)
  void validateModels() {
    Model model = testDataUtil.randomModelObject();

    Response response = createResponseWithBody(serializeObject(model));

    validate("/" + Endpoint.MODELS + "/{model}", Method.GET, response);
  }

  @RepeatedTest(50)
  void validateModerations() {
    ModerationRequest moderationRequest = testDataUtil.randomModerationRequest();

    Request request =
        createRequestWithBody(
            Method.POST, "/" + Endpoint.MODERATIONS.getPath(), serializeObject(moderationRequest));

    Moderation moderation = testDataUtil.randomModeration();

    Response response = createResponseWithBody(serializeObject(moderation));

    validate(request, response);
  }

  @RepeatedTest(50)
  void validateAssistants() {
    CreateAssistantRequest createAssistantRequest = testDataUtil.randomCreateAssistantRequest();

    Request request =
        createRequestWithBody(
            Method.POST,
            "/" + Endpoint.ASSISTANTS.getPath(),
            serializeObject(createAssistantRequest));

    Assistant assistant = testDataUtil.randomAssistant();

    Response response = createResponseWithBody(serializeObject(assistant));

    validate(request, response);
  }

  @RepeatedTest(50)
  void validateThreads() {
    CreateThreadRequest createThreadRequest = testDataUtil.randomCreateThreadRequest();

    Request request =
        createRequestWithBody(
            Method.POST, "/" + Endpoint.THREADS.getPath(), serializeObject(createThreadRequest));

    Thread thread = testDataUtil.randomThread();

    Response response = createResponseWithBody(serializeObject(thread));

    validate(request, response);

    ModifyThreadRequest modifyThreadRequest = testDataUtil.randomModifyThreadRequest();

    request =
        createRequestWithBody(
            Method.POST,
            "/" + Endpoint.THREADS.getPath() + "/{thread_id}",
            serializeObject(modifyThreadRequest));

    validate(request, response);
  }

  @RepeatedTest(50)
  void validateMessages() {
    CreateMessageRequest createMessageRequest = testDataUtil.randomCreateMessageRequest();

    Request request =
        createRequestWithBody(
            Method.POST,
            "/" + Endpoint.THREADS.getPath() + "/{thread_id}/messages",
            serializeObject(createMessageRequest));

    ThreadMessage threadMessage = testDataUtil.randomThreadMessage();

    Response response = createResponseWithBody(serializeObject(threadMessage));

    validate(request, response);

    ThreadMessageFile threadMessageFile = testDataUtil.randomThreadMessageFile();

    response = createResponseWithBody(serializeObject(threadMessageFile));

    validate(
        "/" + Endpoint.THREADS + "/{thread_id}/messages/{message_id}/files/{file_id}",
        Method.GET,
        response);
  }

  @RepeatedTest(50)
  void validateRuns() {
    CreateRunRequest createRunRequest = testDataUtil.randomCreateRunRequest();

    Request request =
        createRequestWithBody(
            Method.POST,
            "/" + Endpoint.THREADS.getPath() + "/{thread_id}/runs",
            serializeObject(createRunRequest));

    // https://github.com/openai/openai-openapi/pull/170
    String reportMessageToIgnore = "Object has missing required properties ([\"thread_id\"]";

    validate(request, reportMessageToIgnore);

    CreateThreadAndRunRequest createThreadAndRunRequest =
        testDataUtil.randomCreateThreadAndRunRequest();

    request =
        createRequestWithBody(
            Method.POST,
            "/" + Endpoint.THREADS.getPath() + "/runs",
            serializeObject(createThreadAndRunRequest));

    validate(request, reportMessageToIgnore);

    ThreadRun threadRun = testDataUtil.randomThreadRun();

    Response response = createResponseWithBody(serializeObject(threadRun));

    validate("/" + Endpoint.THREADS + "/{thread_id}/runs/{run_id}", Method.GET, response);

    ThreadRunStep threadRunStep = testDataUtil.randomThreadRunStep();

    response = createResponseWithBody(serializeObject(threadRunStep));

    validate(
        "/" + Endpoint.THREADS + "/{thread_id}/runs/{run_id}/steps/{step_id}",
        Method.GET,
        response);

    SubmitToolOutputsRequest submitToolOutputsRequest =
        testDataUtil.randomSubmitToolOutputsRequest();

    request =
        createRequestWithBody(
            Method.POST,
            "/" + Endpoint.THREADS.getPath() + "/{thread_id}/runs/{run_id}/submit_tool_outputs",
            serializeObject(submitToolOutputsRequest));

    validate(request);
  }

  private void validate(Request request, Response response, String... reportMessagesToIgnore) {
    ValidationReport report = validator.validate(request, response);
    validateReport(report, reportMessagesToIgnore);
  }

  private void validate(Request request, String... reportMessagesToIgnore) {
    ValidationReport report = validator.validateRequest(request);
    validateReport(report, reportMessagesToIgnore);
  }

  private void validate(
      String path, Method method, Response response, String... reportMessagesToIgnore) {
    ValidationReport report = validator.validateResponse(path, method, response);
    validateReport(report, reportMessagesToIgnore);
  }

  private void validateReport(ValidationReport report, String... reportMessagesToIgnore) {
    List<ValidationReport.Message> errorMessages =
        report.getMessages().stream()
            .filter(
                message -> {
                  // ignore missing only "object"
                  if (message
                      .getMessage()
                      .contains("Object has missing required properties ([\"object\"])")) {
                    return false;
                  }
                  if (Arrays.stream(reportMessagesToIgnore)
                      .anyMatch(toIgnore -> message.getMessage().contains(toIgnore))) {
                    return false;
                  }
                  return message.getLevel() == ValidationReport.Level.ERROR
                      || message.getLevel() == ValidationReport.Level.WARN;
                })
            .toList();
    assertThat(errorMessages)
        .withFailMessage(() -> "Validation error(s): " + errorMessages)
        .isEmpty();
  }

  private Request createRequestWithBody(Method method, String path, String body) {
    return new SimpleRequest.Builder(method, path)
        .withContentType(Constants.JSON_MEDIA_TYPE)
        .withBody(body)
        .build();
  }

  private Response createResponseWithBody(String body) {
    return new SimpleResponse.Builder(200)
        .withContentType(Constants.JSON_MEDIA_TYPE)
        .withBody(body)
        .build();
  }

  private String serializeObject(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  private String serializeObject(Object object, Map<String, Object> additionalFields) {
    try {
      ObjectNode objectNode = objectMapper.valueToTree(object);
      additionalFields.forEach(objectNode::putPOJO);
      return objectMapper.writeValueAsString(objectNode);
    } catch (JsonProcessingException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
