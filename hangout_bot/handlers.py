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

    # user is selecting a campaign to edit
    if (user_data.editing == True and user_data.phase_num == -1):
        user_campaigns = get_user_campaigns(event['user']['email'])
        message = event['message']['text']
        print('Message: ' + message)
        print('name: ' + user_data.user_id)
        print('phase_Num: {}'.format(user_data.phase_num))
        if (not message.isnumeric()):
            return error_message('Selection is not a valid number! Please input a number indicating what campaign you would like to edit.', INVALID_INPUT)
        elif (int(message) < 1 or int(message) > len(user_campaigns)):
            return error_message('Selection is out of bounds!', INVALID_INPUT)
        else:
            print('return')
            user_data.set_accepting_text(False)
            update_user(user_data)
            campaign = convert_entity_to_campaign(user_campaigns[int(message) - 1])
            return create_campaign_overview(campaign)
        # TODO specific edit being made

    # user sent a message to the bot without being prompted
    if user_data.accepting_text != True:
        return error_message('Unexpected input, please respond according to the prompts!', INVALID_INPUT)

    # verify that message is valid
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
        user_data.increment_phase_num()
        user_data.set_accepting_text(True)
        update_user(user_data)
        return start_user_campaign(event)
    if event_action == 'edit_campaign':
        # allow text input(so user can specify which campaign they would like to edit)
        user_data.set_accepting_text(True)
        user_data.set_phase_num(-1)
        user_data.set_editing(True)
        update_user(user_data)

        return start_campaign_edit(event)
    elif event_action == 'quit_campaign':
        # Reset user data to new user status
        user_data.set_phase_num(-1)
        user_data.set_editing(False)
        user_data.set_accepting_text(False)
        user_data.set_campaign_name(None)
        update_user(user_data)

        # Return home prompt for user to act
        return create_home_message(event)
    elif event_action == 'yes_action':
        # confirmation message has parameters containing the value being set
        user_value = event['action']['parameters'][VALUE_INDEX]['value']

        # user is on name phase, ActiveUser is changed and new campaign is added to datastore
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

          if (user_data.phase_num == NEG_LOCATIONS and user_value.lower() == 'n/a'):
              user_value = ''

          # update and prepare the campaign data object to be put back in datastore
          user_campaign_data = update_campaign_data(user_campaign_data,
                                                    user_data.phase_num,
                                                    user_value)
          user_campaign_data.increment_phase_num()

          add_campaign_data(user_campaign_data)

        if (user_data.editing == True and user_data.phase_num == SUBMISSION):
            return create_configure_message(SUBMISSION)

        # current phase complete, append user phase number
        user_data.increment_phase_num()
        user_data.set_accepting_text(True)

         # user is not allowed to input negative locations (Can only put in negative locations if USA is a target)
        user_key = get_user_key(event['user']['email'])
        if (user_data.phase_num == NEG_LOCATIONS and "USA" not in get_user_campaign(user_key).locations):
            # skip negative locations
            user_data.increment_phase_num()

            # update campaign data
            user_key = get_campaign_key(user_data.user_id,
                                      user_data.campaign_name)
            user_campaign_data = get_campaign_data(user_key)
            user_campaign_data.increment_phase_num()
            user_campaign_data.neg_locations = ''
            add_campaign_data(user_campaign_data)

        update_user(user_data)

        # send new configuration response 
        return create_configure_message(user_data.phase_num)
    elif event_action == 'no_action':
        user_data.set_accepting_text(True)
        update_user(user_data)
        
        # re-send current phase num configuration response
        return create_configure_message(user_data.phase_num)
    elif event_action == 'submit':
        delete_datastore_entity(obtain_user_key(event['user']['email']))
        # TODO: post request to website

    elif event_action == 'confirm_edit':
        user_data.campaign_name = event['action']['parameters'][0]['value']
        campaign = get_campaign_data(get_campaign_key(user_data.user_id, user_data.campaign_name))
        user_data.phase_num = campaign.phase_num
        user_data.set_accepting_text(True)
        update_user(user_data)
        return create_configure_message(user_data.phase_num)
    elif event_action == 'back_action':
        return start_campaign_edit(event)