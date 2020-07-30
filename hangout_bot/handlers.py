from messages import *
from datastore import *
from activeuser import *
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

    # user sent a message to the bot without being prompted
    if user_data.accepting_text != True:
        return error_message('Unexpected input, please respond according to the prompts!', INVALID_INPUT)

    # user is selecting a campaign to edit (home page prompt)
    if (user_data.editing == True and user_data.phase_num == INACTIVE):
        user_campaigns = get_user_campaigns(event['user']['email'])
        message = event['message']['text']

        # message is unrecognizable
        if (not message.isnumeric()):
            return error_message('Selection is not a valid number! Please input a number indicating what campaign you would like to edit.', INVALID_INPUT)
        elif (int(message) < 1 or int(message) > len(user_campaigns)):
            return error_message('Selection is out of bounds!', INVALID_INPUT)
        else:
            print('return')
            user_data.set_accepting_text(False)
            update_user(user_data)
            campaign = convert_entity_to_campaign(user_campaigns[int(message) - 1])
            return create_campaign_overview(campaign, False)

    # user is selecting a setting to edit before submitting
    elif (user_data.editing == True and user_data.phase_num == SUBMISSION):
        # user is editing a specific setting of the campaign
        user_campaigns = get_user_campaigns(event['user']['email'])
        message = event['message']['text']
        
        # message is unrecognizable
        if (not message.isnumeric()):
            return error_message('Selection is not a valid number! Please input a number indicating what setting you would like to edit.', INVALID_INPUT)
        
        # selection is out of bounds
        elif ((int(message) - 1) < NAME or (int(message) - 1) > SUBMISSION - 1):
            return error_message('Selection is out of bounds!', INVALID_INPUT)

        else:
            # user input a valid setting integer, can now configure the setting
            user_data.set_accepting_text(True)
            user_data.set_phase_num(int(message) - 1)
            update_user(user_data)
            return create_configure_message(user_data.phase_num, True)

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
        # user is creating a new DSA campaign

        # append user phase_num to 0 and accepting input to True
        # user is prompted to enter a campaign name
        user_data.increment_phase_num()
        user_data.set_accepting_text(True)
        update_user(user_data)
        return start_user_campaign(event)
    
    elif event_action == 'edit_campaign':
        # user would like to edit an existing campaign

        # allow text input(so user can specify which campaign they would like to edit)
        user_data.set_accepting_text(True)
        user_data.set_phase_num(INACTIVE)
        user_data.set_editing(True)
        update_user(user_data)

        return start_campaign_edit(event)
    
    elif event_action == 'quit_campaign':
        # user has chosen to quit campaign configuration

        # Reset user data to new user status
        user_data.set_phase_num(INACTIVE)
        user_data.set_editing(False)
        user_data.set_accepting_text(False)
        user_data.set_campaign_name(None)
        update_user(user_data)

        # Return home prompt for user to act
        return create_home_message(event)
    
    elif event_action == 'yes_action':
        print('b {}'.format(user_data.phase_num))
        # user input setting is correct
        # confirmation message has parameters containing the value being set
        user_value = event['action']['parameters'][VALUE_INDEX]['value']

        # user is on name phase, ActiveUser is changed and new campaign is added to datastore
        if user_data.phase_num == NAME:
          # add new campaign with event id and name from confirmation event
          if user_data.editing != True:
            # user is not editing and this is a new campaign
            add_new_campaign(event['user']['email'], user_value)
          else:
            # user is editing and campaign name must be modified
            campaign_key = get_campaign_key(user_data.user_id,
                                      user_data.campaign_name)
            user_campaign_data = get_campaign_data(campaign_key)
            user_campaign_data = update_campaign_data(user_campaign_data,
                                                    user_data.phase_num,
                                                    user_value)
            # delete old datastore entity
            delete_datastore_entity(campaign_key)

            add_campaign_data(user_campaign_data)
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


        # current phase complete, append user phase number
        user_data.increment_phase_num()

        user_key = get_user_key(event['user']['email'])
        campaigns = get_user_current_campaign(user_key)

        if (user_data.editing == True):
            user_data.phase_num = SUBMISSION

        # User is on the submission phase
        if (user_data.phase_num == SUBMISSION):
            user_data.set_accepting_text(False)
            update_user(user_data)
            return create_campaign_overview(campaigns, True)

        user_data.set_accepting_text(True)

         # check if user is allowed to input negative locations (Can only put in negative locations if USA is a target)
        if (user_data.phase_num == NEG_LOCATIONS and "USA" not in campaigns.locations):
            # user is not allowed to add negative locations

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
        
        # get campaign data for overview
        user_key = get_campaign_key(user_data.user_id,
                                      user_data.campaign_name)
        user_campaign_data = get_campaign_data(user_key)

        print('a {}'.format(user_data.phase_num))

        # send new configuration response 
        return (create_configure_message(user_data.phase_num, user_data.editing) if user_data.phase_num < SUBMISSION else create_campaign_overview(user_campaign_data, True))
    
    elif event_action == 'no_action':
        # user would like to re-enter setting value
        user_data.set_accepting_text(True)
        update_user(user_data)
        
        # re-send current phase num configuration response
        return create_configure_message(user_data.phase_num, user_data.editing)
    
    elif event_action == 'submit':
        # TODO: post request to web page
        print('submitting')
    
    elif event_action == 'confirm_edit':
        # User has viewed the WIP campaign and has confirmed to edit
        user_data.campaign_name = event['action']['parameters'][VALUE_INDEX]['value']
        campaign = get_campaign_data(get_campaign_key(user_data.user_id, user_data.campaign_name))
        user_data.phase_num = campaign.phase_num
        user_data.set_editing(False)
        # If campaign is complete, show overview message
        if (user_data.phase_num == SUBMISSION):
            user_data.set_accepting_text(False)
            update_user(user_data)
            return create_campaign_overview(campaign, True)
        
        # user is prompted to input settings
        user_data.set_accepting_text(True)
        update_user(user_data)
        return create_configure_message(user_data.phase_num, False)
    
    elif event_action == 'back_action':
        # if user is not happy with the campaign they have chosen, they can go back
        return start_campaign_edit(event)
    
    elif event_action == 'back_submission':
        # user cancelled editing a submission
        campaign = get_campaign_data(get_campaign_key(user_data.user_id, user_data.campaign_name))
        return create_campaign_overview(campaign, True)
    
    elif event_action == 'edit_campaign_settings':
        
        # user has chosen to edit a specific setting of the campaign before submission
        user_data.set_editing(True)
        user_data.set_accepting_text(True)
        update_user(user_data)
        return create_setting_list()