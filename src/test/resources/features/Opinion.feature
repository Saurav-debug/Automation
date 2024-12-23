Feature: Translate and analyze articles from the Opinion section

  @Exercise
  Scenario: Fetch and translate article titles on multiple browsers
    Given User navigates to the Opinion section page
    When User retrieves the first five article titles
    Then The titles are translated to English
    And Repeated words in titles are logged


