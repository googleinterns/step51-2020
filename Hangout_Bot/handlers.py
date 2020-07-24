from messages import *
from datastore import *

def handle_join(event):
    # Register user as active
    add_user(event)
    return create_join_message(event)

def handle_message(event):
    """Determine what logic to execute based upon message event data.
    Args:
      event: A dictionary with the event data.
    Returns:
      error dictionary message if event message is invalid
      confirmation dictionary message if event message is valid
    """
    user_data = convert_entity_to_user(get_user_data(obtain_user_key(event['user']['email'])))
    
    if user_data.phase_num == -1 or user_data.accepting_input != True:
        return error_message('Unexpected input, please respond according to the prompts!', user_data.phase_num)

    error_msg = error_handler(event['message']['text'], user_data.phase_num)
    if error_msg != True:
        return error_message(error_msg, user_data.phase_num)

    return create_confirmation_message(event, user_data.phase_num)


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
        # TODO: set user phase_num to 0

        return start_user_campaign(event)
    if event_action == 'edit_campaign':
        # return edit_user_campaign(event)
        print('editing')
    elif event_action == 'cancel_campaign':
        print('cancelling')
    elif event_action == 'yes_action':
        # TODO: replace active_users with datastore integration
        print('yes button clicked')
    elif event_action == 'no_action':
        print('no button clicked')