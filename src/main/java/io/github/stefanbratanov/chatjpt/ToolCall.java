package io.github.stefanbratanov.chatjpt;

public record ToolCall(String id, String type, Function function) {

  /**
   * @param name The name of the function to call.
   * @param arguments The arguments to call the function with, as generated by the model in JSON
   *     format. Note that the model does not always generate valid JSON, and may hallucinate
   *     parameters not defined by your function schema. Validate the arguments in your code before
   *     calling your function.
   */
  public record Function(String name, String arguments) {}

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private String id;
    private String type;
    private Function function;

    /**
     * @param id The ID of the tool call.
     */
    public Builder id(String id) {
      this.id = id;
      return this;
    }

    /**
     * @param type The type of the tool.
     */
    public Builder type(String type) {
      this.type = type;
      return this;
    }

    /**
     * @param function The function that the model called.
     */
    public Builder function(Function function) {
      this.function = function;
      return this;
    }

    public ToolCall build() {
      if (id == null) {
        throw new IllegalStateException("id must be set");
      }
      if (type == null) {
        throw new IllegalStateException("type must be set");
      }
      if (function == null) {
        throw new IllegalStateException("function must be set");
      }
      return new ToolCall(id, type, function);
    }
  }
}