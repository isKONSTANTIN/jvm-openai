package io.github.stefanbratanov.jvm.openai;

/**
 * Usage statistics for the completion request.
 *
 * @param completionTokensDetails Breakdown of tokens used in a completion.
 */
public record CompletionUsage(
    int completionTokens,
    int promptTokens,
    int totalTokens,
    CompletionTokensDetails completionTokensDetails,
    PromptTokensDetails promptTokensDetails) {

  /**
   * @param reasoningTokens Tokens generated by the model for reasoning.
   */
  public record CompletionTokensDetails(
      Integer reasoningTokens,
      Integer audioTokens,
      Integer acceptedPredictionTokens,
      Integer rejectedPredictionTokens) {}

  public record PromptTokensDetails(Integer cachedTokens, Integer audioTokens) {}
}
