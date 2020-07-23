from messages import *
from datastore import *

def handle_message(event):
    """Determine what logic to execute based upon message event data.
    Args:
      event: A dictionary with the event data.
    Returns:
      error dictionary message if event message is invalid
      confirmation dictionary message if event message is valid
    """
    user_id = event['user']['email']

    # if active_users.get(user_id) == None or active_users.get(user_id)[EXPECT_TEXT_INPUT] == False:
      #  return error_message('Unexpected input, please respond according to the prompts!', -1)
    
    # error_msg = error_handler(event['message']['text'], active_users.get(user_id)[PHASE_NUM_INDEX])

    if (error_msg != True):
        return error_message(error_msg, 0) # active_users.get(user_id)[PHASE_NAME_INDEX])

    return create_confirmation_message(event, 0)# active_users.get(user_id)[PHASE_NUM_INDEX])


def handle_button_click(event):
    """Determine what logic to execute based upon button event data.
    Args:
      event: A dictionary with the event data.
    Returns:
      appropriate dictionary response based on the button clicked
    """
    event_action = event['action']['actionMethodName']
    user_id = event['user']['email']

    if event_action == 'start_campaign':
        add_user(event['user']['email'], '', )
        return start_user_campaign(event)
    # TODO: REMOVE curr_phase_num = active_users.get(event['user']['email'])[PHASE_NUM_INDEX]
    if event_action == 'edit_campaign':
        # return edit_user_campaign(event)
        print('editing')
    elif event_action == 'cancel_campaign':
        print('cancelling')
    elif event_action == 'yes_action':
        # TODO: replace active_users with datastore integration
        if int(event['action']['parameters'][0]) != active_users.get(user_id)[PHASE_NUM_INDEX]:
            return error_message('You clicked a card that is no longer active!', -1)
        # yes_action button click indicates that user is ready to move on
        active_users.get(event['user']['email'])[EXPECT_TEXT_INPUT] = True
        active_users.get(event['user']['email'])[PHASE_NUM_INDEX] = curr_phase_num + 1
        return create_configure_message(curr_phase_num + 1)
    elif event_action == 'no_action':
        if int(event['action']['parameters'][0]) != active_users.get(user_id)[PHASE_NUM_INDEX]:
            return error_message('You clicked a card that is no longer active!', -1)
        # no_action button click indicates that the user is not ready to move on
        active_users.get(event['user']['email'])[EXPECT_TEXT_INPUT] = True
        return create_configure_message(curr_phase_num)