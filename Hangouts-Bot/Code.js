/**
 * Responds to a MESSAGE event in Hangouts Chat.
 *
 * @param {Object} event the event object from Hangouts Chat
 */
function onMessage(event) {
  var name = "";

  if (event.space.type == "DM") {
    name = "You";
  } else {
    name = event.user.displayName;
  }
  var message = name + " said \"" + event.message.text + "\"";

  return { "text": message };
}

//map containing phases for DSA campaign creation.
let phase_map = new Map([
    [0, 'campaign_type'],
    [1, 'sim_name'],
    [2, 'start_date'],
    [3, 'end_date'],
    [3, 'budget'],
    [4, 'country'],
    [5, 'region'],
    [6, 'domain'],
    [7, 'target_page'],
    [8, 'ad_text'],
    [9, 'cpc']
]);

//boolean indicating if the creation phase has started.
let creating = false;

// variable to keep track of DSA creation phase.
let phase_num = 0;

//POST query string to be built by bot.
let query_string = '';

/**
 * Starts the configuration process for the DSA Campaign.
 *
 * @param {Object} event the event object from the Hangouts Chat
 */
function startConfiguration(message) {
   
}

/**
 * Validates each message based on parameters.
 *
 * @param   message       message being validated
 * @param   requiredInput values that the message must match.
 * @returns               boolean representing if message is valid. 
 */
function validateAnswer(message, requiredInput) {
  for (index = 0; index < requiredInput.length; index++) { 
    if (requiredInput[index] === message) {
      return true; 
    }
  } 
  return false;
}

/**
 * Responds to an ADDED_TO_SPACE event in Hangouts Chat.
 *
 * @param {Object} event the event object from Hangouts Chat
 */
function onAddToSpace(event) {
  var message = "";

  if (event.space.singleUserBotDm) {
    message = "Hi, " + event.user.displayName + "! Type @DSA Campaign Uplift Bot to begin configuring your estimate!";
  }
  
  
  if (event.message) {
    // Bot added through @mention.
    message = message + " and you said: \"" + event.message.text + "\"";
  }

  return { "text": message };
}

/**
 * Responds to a REMOVED_FROM_SPACE event in Hangouts Chat.
 *
 * @param {Object} event the event object from Hangouts Chat
 */
function onRemoveFromSpace(event) {
  console.info("Bot removed from ",
      (event.space.name ? event.space.name : "this chat"));
}

