from messages import *
from datastore import *
from userdata import *
from constant import *

def handle_join(event):
    """Handles 'ADDED_TO_SPACE' event, returns customized join message
       based on event parameter. Registers new user as ActiveUser to datastore
    Args:
      event: 
        event dictionary containing ADDED_TO_SPACE event.
    Returns:
      dict
        Join response dictionary
    """

    # Register user as active
    add_new_user(event)

    # return the join message
    return create_join_message(event)

def handle_message(event):
    """Determines if message is valid and sends confirmation
       prompt or error prompt
    Args:
      event: event dictionary containing message.
    Returns:
      dict
        error dictionary response if event message is invalid
      dict
        confirmation response if event message is valid
    """

    user_data = get_user_data(get_user_key(event['user']['email']))
    print(user_data.accepting_text)
    if user_data.accepting_text != True:
        return error_message('Unexpected input, please respond according to the prompts!', INVALID_INPUT)

    error_msg = error_handler(event, user_data.phase_num)
    if error_msg != True:
        return error_message(error_msg, user_data.phase_num)
    
    # Confirmation prompt does not require text input
    user_data.accepting_text = False
    
    # update the user in datastore to reflect changes
    update_user(user_data)

    return create_confirmation_message(event, user_data.phase_num, user_data.editing)


def handle_button_click(event):
    """Handles response based on action of 'CARD_CLICKED'
    Args:
      event:
        The 'CARD_CLICKED' being parsed by handler
    Returns:
      dict
        dictionary containing event response
    """

    event_action = event['action']['actionMethodName']
    user_data =  get_user_data(get_user_key(event['user']['email']))

    if event_action == 'start_campaign':
        # append user phase_num to 0 and accepting input to True
        # user is prompted to enter a campaign name
        user_data.append_phase_num()
        user_data.set_accepting_text(True)
        update_user(user_data)
        return start_user_campaign()
    if event_action == 'edit_campaign':
        # return edit_user_campaign(event)
        # set user editing to True
        # TODO
        print('editing')
    elif event_action == 'cancel_campaign':
        # TODO
        print('cancelling')
    elif event_action == 'yes_action':
        # confirmation message has parameters containing the value being set
        user_value = event['action']['parameters'][VALUE_INDEX]['value']

        # user is on name phase, UserData is changed and new campaign is added to datastore
        if user_data.phase_num == NAME:
          # add new campaign with event id and name from confirmation event
          add_new_campaign(event['user']['email'], user_value)
          user_data.campaign_name = user_value
          update_user(user_data)
        else:
          # get the user campaign being modified from datastore
          user_key = get_campaign_key(user_data.user_id,
                                      user_data.campaign_name)
          user_campaign_data = get_campaign_data(user_key)

          # update and prepare the campaign data object to be put back in datastore
          user_campaign_data = update_campaign_data(user_campaign_data,
                                                    user_data.phase_num,
                                                    user_value)
          add_campaign_data(user_campaign_data)

        # current phase complete, append user phase number
        user_data.append_phase_num()
        user_data.set_accepting_text(True)
        update_user(user_data)

        # send new configuration response 
        return create_configure_message(user_data.phase_num)
    elif event_action == 'no_action':
        user_data.set_accepting_text(True)
        update_user(user_data)
        
        # re-send current phase num configuration response
        return create_configure_message(user_data.phase_num)