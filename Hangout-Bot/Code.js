/**
 * Responds to a MESSAGE event in Hangouts Chat.
 *
 * @param {Object} event the event object from Hangouts Chat
 */
function onMessage(event) {
  var name = "";
  
  if (event.space.type == "DM") {
    name = "You" + phase_num;
  } else {
    name = event.user.displayName;
  }
  var message = name + " said \"" + event.message.text + phase_num + "\"";
  phase_num++;
  
  return { "text": message };
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

