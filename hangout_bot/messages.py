from constant import *
from clientserver import *
from decimal import *
import re

# phase number (key) : [phase name, phase specific prompt message]
# used to generate phase specific messages

PHASE_DICTIONARY = {
  PHASE_NUM.KEYWORD_CAMPAIGN: ['keyword campaign', 'Please select the number corresponding ' +
  'to the desired keyword campaign being associated with your campaign.'],
  PHASE_NUM.NAME: ['name',
      'Please input the name that will be associated with your dynamic ' +
      'search ad campaign estimate.'],
  PHASE_NUM.START_DATE: ['start date',
      'Please input the date that your campaign estimate starts. <b>Must ' +
      'be in the form mm-dd-yyyy.</b>'],
  PHASE_NUM.END_DATE: ['end date',
      'Please input the date that your campaign estimate will end. <b>Must ' +
      'be in the form mm-dd-yyyy.</b>'],
  PHASE_NUM.DAILY_BUDGET: ['daily budget', 'Please input the amount you are willing ' +
      'to spend on the campaign each day.'],
  PHASE_NUM.LOCATIONS: ['locations', 'Please enter a comma separated list of state ' +
      'codes that your ad will target. Must be in the form of a valid US ' +
      'State abbreviation (Ex: \"CA, TX, MN\") (Must follow this form!). ' +
      'Enter \"USA\" if you would like to target the entire US.'],
  PHASE_NUM.NEG_LOCATIONS: ['negative locations', 'Are there any locations that your ' +
      'ad will not target? Ex: CA, MN, PA (Must follow this format!' + 
      '). Input N/A if not applicable.'],
  PHASE_NUM.DOMAIN: ['domain', 'What is the domain of the webpage being advertised? ' +
      '(Ex: http://www.google.com/)'],
  PHASE_NUM.TARGETS: ['targets', 'What are target pages of your domain ' +
      'that you would like to specifically advertise? Multiple ' +
      'target pages can be input as a comma separated list. ' +
      '(Ex: http://www.google.com/page1/, http://www.google.com/page2/)'],
  PHASE_NUM.MANUAL_CPC: ['cost per click', 'What is the desired cost per click? ' +
      'The amount specified here will be how much you pay everytime an ' +
      'ad is clicked.'],
  PHASE_NUM.AD_TEXT: ['ad text', 'What text should appear on your ad.']
}


# [Error Handling]

def error_handler(event, phase_num):
    """Returns a True if event is valid or
       string specifying specific error if
       event is invalid.

    Args:
      event: the dictionary event being checked
      phase_num: the phase number used to check validity of message

    Returns:
      True
        if no error is found
      Str
        specifes the specific error if an error is raised
    """

    # error map (PHASE_DICTIONARY key) : error handling reference
    ERROR_DICTIONARY = {
      PHASE_NUM.NAME: '',  # name cannot be an empty string or length 0
      PHASE_NUM.START_DATE: 'mm-dd-yyyy',   # start date must be in the form mm-dd-yyyy
      PHASE_NUM.END_DATE: 'mm-dd-yyyy',   # end date must be in the form mm-dd-yyyy
      PHASE_NUM.DAILY_BUDGET: 0.01,   # daily budget must be at least 1 cent.
      # 6/7 will be validated using python URL validator
      PHASE_NUM.MANUAL_CPC: 0.01,   # manual CPC must be at least 1 cent.
      PHASE_NUM.AD_TEXT: '',
      PHASE_NUM.LOCATIONS: ''
    }

    # allows each success message to be changed at once, if necessary
    success = True
    message = event['message']['text']

    if phase_num == PHASE_NUM.VIEWING_KEYWORD_CAMPAIGNS:
        campaigns = get_keyword_campaigns(event['user']['email'])
        if (not message.isdigit()):
            return error_message('Selection is not a valid number! Please input a number indicating what campaign you would like to view.', INVALID_INPUT)
        elif (int(message) < 1 or int(message) > len(campaigns)):
            return error_message('Selection is out of bounds!', INVALID_INPUT)
        return success  
    # phase 0: name
    if phase_num == PHASE_NUM.KEYWORD_CAMPAIGN:
        kc_campaigns = get_keyword_campaigns(None)
        try:
          selection = int(message)
          if (selection < 1 or selection > len(kc_campaigns)): 
              return 'Selection is out of bounds! Please enter a valid value.'
          else:
              return success
        except ValueError:
          return 'Selection is not a numeric value! Please enter a valid number corresponding to the keyword campaign of interest.'
    elif phase_num == PHASE_NUM.NAME:
        if (len(message) > 0 and message != ERROR_DICTIONARY[PHASE_NUM.NAME]):
            user_key = get_campaign_key(event['user']['email'], message)
            if (get_campaign_data(user_key) == None):
                return success
            else:
                return 'The campaign name, \"{}\", already exists!'.format(message)
        else:
          return 'Name is not valid!'

    # phase 1: start date, phase 2: end date
    elif phase_num == PHASE_NUM.START_DATE or phase_num == PHASE_NUM.END_DATE:
        date_parameters = message.split('-')

        # Expected date_parameters [mm, dd, yyyy]

        if len(date_parameters) == 3:
            if (int(date_parameters[0]) >= JANUARY) and (int(date_parameters[0]) <= DECEMBER):
                if ((int(date_parameters[1]) > 0) and (int(date_parameters[1]) <= LAST_DAY_MONTH - 1)) or ((int(date_parameters[0]) % 2 == 0) and (int(date_parameters[1]) == LAST_DAY_MONTH)):
                    if ((int(date_parameters[2]) >= 2000) and (int(date_parameters[2]) <= 2099)):
                        return success
                    else:
                        return 'Year can only be between 2000 and 2099!'
                else:
                    return 'Day must be between 1 and 31! If you entered 31 as a day, make sure the month has 31 days!'
            else:
                return 'Month must be between 1 and 12!'
        return 'Date is not in the right format! Must be in the form mm-dd-yyyy!'

    # phase 3: daily budget, phase 8: manual CPC
    elif phase_num == PHASE_NUM.DAILY_BUDGET or phase_num == PHASE_NUM.MANUAL_CPC:
        attribute_name = 'Daily budget' if phase_num == PHASE_NUM.DAILY_BUDGET else 'Manual CPC (Cost Per Click)'
        message = message[0:].strip()
        try:
          float(message)
          if (Decimal(message) >= ERROR_DICTIONARY[PHASE_NUM.DAILY_BUDGET]): 
            return success
          else:
            return '{} must be at least $0.01!'.format(attribute_name)
        except:
           return '{} must be a valid decimal value! Ex: 34.00'.format(attribute_name)
    
    # phase 4: locations, phase 5: negative locations
    elif phase_num == PHASE_NUM.LOCATIONS or phase_num == PHASE_NUM.NEG_LOCATIONS:
        if (phase_num == PHASE_NUM.NEG_LOCATIONS and message.lower() == 'n/a'):
            return success
        locations = message.split(',')
        for idx in range(len(locations)):
            # trim white space
            locations[idx] = locations[idx].strip()
            if phase_num == PHASE_NUM.NEG_LOCATIONS:
                campaign_name = get_user_data(get_user_key(event['user']['email'])).campaign_name
                campaign_key = get_campaign_key(event['user']['email'], campaign_name)
                location_data = get_campaign_data(campaign_key).locations.split(',')
                
                # Remove whitespace from all location entries
                map(str.strip, location_data)
                if locations[idx] in location_data:
                    return '{} is already a targetted location, it cannot be excluded.'.format(locations[idx])
                elif (locations[idx] == 'USA'):
                    return 'You cannot add USA as a negative location!'
            # user entered USA as location, other locations are ignored
            if (locations[idx] == 'USA' and phase_num != 5):
                return success
            
            regex = re.compile('[A-Za-z][A-Za-z]')
            if not re.match(regex, locations[idx]):
              attribute = 'Locations' if phase_num == 4 else 'Negative locations'
              return attribute +  ' must be a two letter code! Ex: CA, TX, MN, PA'
        
        if (len(locations) == 0):
            return 'Locations must be a comma separated list of state codes. Ex: CA, TX, MN, PA'
        return success
    # phase 6: domain, phase 7: target page(s)
    elif phase_num == PHASE_NUM.DOMAIN or phase_num == PHASE_NUM.TARGETS:
        # regex string being used to validate the URL
        url_regex = re.compile(
            r'^(?:http|ftp)s?://' # http:// or https://
            r'(?:(?:[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?\.)+(?:[A-Z]{2,6}\.?|[A-Z0-9-]{2,}\.?)|' #domain...
            r'localhost|' #localhost...
            r'\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})' # ...or ip
            r'(?::\d+)?' # optional port
            r'(?:/?|[/?]\S+)$', re.IGNORECASE)

        # format response string based on what the phase number is
        attribute_name = 'domain' if phase_num == 6 else 'target pages'
        failed = 'URL is not valid, please ensure that you specified the correct {}!'.format(attribute_name)
        # TARGETS can be a comma separated list
        if (phase_num == PHASE_NUM.TARGETS):
            domains = message.split(',')
            for idx in range(len(domains)):
                # trim white space
                domains[idx] = domains[idx].strip()
                if not re.match(url_regex, domains[idx]):
                    return failed
            return success
        
        return success if re.match(url_regex, message) else failed
    elif phase_num == PHASE_NUM.AD_TEXT:
        if (len(message) > 0 and message != ERROR_DICTIONARY[PHASE_NUM.NAME]):
            return success
        else:
          return 'Ad text is not valid! Cannot be an empty value!'

def create_keyword_campaign_list(user_id, editing, selecting_keyword_campaign):
    keyword_campaign_list = ''
    userid = user_id if selecting_keyword_campaign else None
    keyword_json = get_keyword_campaigns(userid)

    for i in range(len(keyword_json)):
        keyword_campaign_list = keyword_campaign_list + '<b>{}.</b> {}<br>'.format((i + 1), keyword_json[i]['name'])
    message = "Please send a number corresponding to the desired keyword campaign.<br>{}".format(keyword_campaign_list)
    
    if len(keyword_json) == 0:
        message = "There are no active keyword campaigns, please create a campaign when a valid keyword campaign exists."
    return {
              "actionResponse": {
                "type": "UPDATE_MESSAGE" if not editing else "NEW_MESSAGE"
              },
              "cards": [
                {
                  "header": build_header('Editing'),
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": message
                          }
                        }
                      ]
                    },
                    {
                      "widgets": [
                        {
                          "buttons": [
                            {
                              "textButton": {
                                "text": "QUIT",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "quit_campaign",
                                  }
                                }
                              }
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }

def create_campaign_list(user_id, keyword_campaign_id, editing):
    """Shows list of campaigns belonging to current user
    Args:
      user_id:
        user id being used to get campaign list
      keyword_campaign_id:
        keyword campaign id used to get campaign list
      editing:
        determines if message is used for editing or for submitting
    Returns:
      dict
        dictionary contains start campaign config message
    """
    campaigns = get_dsa_campaigns(user_id, keyword_campaign_id)
    
    if len(campaigns) == 0:
        return error_message('There are no DSA Campaigns associated with this keyword campaign.', INVALID_INPUT)
    campaign_list = ''
    for i in range(len(campaigns)):
        campaign_list = campaign_list + '<b>{}.</b> {}<br>'.format(i + 1, campaigns[i].name)

    return {
              "actionResponse": {
                "type": "NEW_MESSAGE" if not editing else "UPDATE_MESSAGE"
              },
              "cards": [
                {
                  "header": build_header('Viewing'),
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": "Please send a number corresponding to the campaign you would like to view.<br>" + campaign_list
                          }
                        }
                      ]
                    },
                    {
                      "widgets": [
                        {
                          "buttons": [
                            {
                              "textButton": {
                                "text": "BACK",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "back_submission" if editing else "quit_campaign"
                                  }
                                }
                              }
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }

def create_setting_list():
    """Formats the introduction message response
    Args:
      None
    Returns:
      dict
        dictionary contains start campaign config message
    """

    campaign_list = ''
    for i in range(PHASE_NUM.SUBMISSION):
        campaign_list = campaign_list + '<b>{}.</b> {}<br>'.format(i + 1, PHASE_DICTIONARY.get(i)[PHASE_NAME_INDEX].capitalize())

    return {
              "actionResponse": {
                "type": "UPDATE_MESSAGE"
              },
              "cards": [
                {
                  "header": build_header('Editing'),
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": "Please send a number corresponding to the setting " +
                              "you would like to edit.<br>" + campaign_list
                          }
                        }
                      ]
                    },
                    {
                      "widgets": [
                        {
                          "buttons": [
                            {
                              "textButton": {
                                "text": "QUIT",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "quit_campaign",
                                  }
                                }
                              }
                            },
                            {
                              "textButton": {
                                "text": "BACK",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "back_submission",
                                  }
                                }
                              }
                            },
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }


# [Message Generating Functions]

def error_message(error_msg, phase_num):
    """Formats an error message response for invalid input
    Args:
      error_msg:
        the specifc error message being displayed
      phase_num:
        the phase number that raised the error
    Returns:
      dict
        dictionary contains error response message
    """

    error = "<font color=\"#ff0000\">ERROR</font>: {}<br><b>Please send a valid value for your campaign {}!</b>".format(error_msg, PHASE_DICTIONARY.get(phase_num)[PHASE_NAME_INDEX]) if phase_num != PHASE_NUM.INACTIVE else "<font color=\"#ff0000\">ERROR</font>: <b>{}</b>".format(error_msg)
    return {
              "cards": [
                {
                  "header": build_header('Alert'),
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": error
                          }
                        }
                      ]
                    },
                    {
                      "widgets": [
                        {
                          "buttons": [
                            {
                              "textButton": {
                                "text": "QUIT",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "quit_campaign"
                                  }
                                }
                              }
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }

def create_campaign_overview(campaign_data, submission):
    """Returns a campaign overview for users to view,
    Buttons on message vary based on submission boolean

    Args:
      campaign_data:
        CampaignData used to populate campaign data
      submission:
        boolean representing if overview is for submission
    Returns:
      dict
        dictionary containing overview message
    """    

    not_set = 'None'
    return {
      "actionResponse": {
        "type": "UPDATE_MESSAGE" if submission else "NEW_MESSAGE"
      },
      "cards": [
        {
          "header": build_header('Editing'),
          "sections": [
            {
              "widgets": [
                {
                  "keyValue": {
                    "topLabel": "Keyword Campaign ID",
                    "content": campaign_data.keyword_campaign_id if campaign_data.keyword_campaign_id != '' else not_set,
                    "icon": "STAR"
                  }
                },
                {
                  "keyValue": {
                    "topLabel": "Campaign Name",
                    "content": campaign_data.name if campaign_data.name != '' else not_set,
                    "icon": "STAR"
                  }
                },
                {
                  "keyValue": {
                    "topLabel": "Start Date",
                    "content": campaign_data.start_date if campaign_data.start_date != '' else not_set,
                    "icon": "INVITE"
                  }
                },
                {
                  "keyValue": {
                    "topLabel": "End Date",
                    "content": campaign_data.end_date if campaign_data.end_date != '' else not_set,
                    "icon": "INVITE"
                  }
                },
                {
                  "keyValue": {
                    "topLabel": "Daily Budget",
                    "content": '${}'.format(campaign_data.daily_budget) if campaign_data.daily_budget != 0.0 else not_set,
                    "icon": "DOLLAR"
                  }
                },
                {
                  "keyValue": {
                    "topLabel": "Cost Per Click",
                    "content": '${}'.format(campaign_data.manual_CPC) if campaign_data.manual_CPC != 0.0 else not_set,
                    "icon": "DOLLAR"
                  }
                },
                {
                  "keyValue": {
                    "topLabel": "Domain",
                    "content": campaign_data.domain if campaign_data.domain != '' else not_set,
                    "icon": "BOOKMARK"
                  }
                },
                {
                  "keyValue": {
                    "topLabel": "Target Pages",
                    "content": campaign_data.targets if campaign_data.targets != '' else not_set,
                    "icon": "BOOKMARK"
                  }
                },
                {
                  "keyValue": {
                    "topLabel": "Locations",
                    "content": campaign_data.locations if campaign_data.locations != '' else not_set,
                    "icon": "MAP_PIN"
                  }
                },
                {
                  "keyValue": {
                    "topLabel": "Negative Locations",
                    "content": campaign_data.neg_locations if campaign_data.neg_locations != '' else not_set,
                    "icon": "MAP_PIN"
                  }
                },
                {
                  "keyValue": {
                    "topLabel": "Ad Text",
                    "content": campaign_data.ad_text if campaign_data.ad_text != '' else not_set,
                    "icon": "DESCRIPTION"
                  }
                }
              ]
            },
            {
              "widgets": [
                {
                  "buttons": add_overview_buttons(campaign_data, submission)
                }
              ]
            }
          ]
        }
      ]
    }


def add_overview_buttons(campaign_data, submission):
    """Returns list of submission specific buttons (Submit, Edit, Quit).
    If not submission, then different list of buttons are
    returned (Edit (not same as submit), Back)

    Args:
      campaign_data:
        CampaignData used to populate button values
      submission:
        boolean representing if overview is for submission
    Returns:
      list
        list of dictionaries containing buttons
    """

    button_list = []
    if not submission:
        button_list.append(
          {
            "textButton": {
              "text": "EDIT",
              "onClick": {
                "action": {
                  "actionMethodName": "confirm_edit",
                  "parameters": [
                    {
                      "key": "campaign_name",
                      "value": campaign_data.name
                    }
                  ]
                }
              }
            }
          }
        )
        button_list.append(
          {
            "textButton": {
              "text": "BACK",
              "onClick": {
                "action": {
                  "actionMethodName": "back_action"
                }
              }
            }
          }
        )
    else:
        button_list.append(
          {
            "textButton": {
              "text": "SUBMIT",
              "onClick": {
                "action": {
                  "actionMethodName": "submit"
                }
              }
            }
          }
        )
        button_list.append(
          {
            "textButton": {
              "text": "QUIT",
              "onClick": {
                "action": {
                  "actionMethodName": "quit_campaign"
                }
              }
            }
          }
        )
        button_list.append(
           {
            "textButton": {
              "text": "EDIT SETTINGS",
              "onClick": {
                "action": {
                  "actionMethodName": "edit_campaign_settings"
                }
              }
            }
          }
        )
    return button_list

def create_confirmation_message(message, phase_num, editing):
    """Formats a confirmation message response for valid input
    Args:
      message:
        the specifc message that has passed error
        checking and is ready to be user confirmed
      phase_num:
        the phase number that will be used
        to confirm the input value
    Returns:
      dict
        dictionary contains confirmation prompt
    """

    message_value = message
    if (phase_num == PHASE_NUM.KEYWORD_CAMPAIGN):
        message_value = get_keyword_campaigns(None)[int(message) - 1]['name']

        # store the index of the keyword campaign being chosen
        message = int(message) - 1

    return {
              "cards": [
                {
                  "header": build_header('Editing'),
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": "You picked <b>\"{}\"</b> for your campaign {}, is this correct?".format(message_value, PHASE_DICTIONARY.get(phase_num)[PHASE_NAME_INDEX])
                          }
                        }
                      ]
                    },
                    {
                      "widgets": [
                        {
                          "buttons": [
                            {
                              "textButton": {
                                "text": "NO",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "no_action",
                                    "parameters": [
                                      {
                                        "key": "phase_num",
                                        "value": phase_num
                                      },
                                      {
                                        "key": "editing",
                                        "value": editing
                                      }
                                    ]
                                  }
                                }
                              }
                            },
                            {
                              "textButton": {
                                "text": "YES",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "yes_action",
                                    "parameters": [
                                      {
                                        "key": "input_data",
                                        "value": message
                                      },
                                      {
                                        "key": "editing",
                                        "value": editing
                                      }
                                    ]
                                  }
                                }
                              }
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }

def start_campaign_edit(event):
    """Formats the introduction message response
    Args:
      None
    Returns:
      dict
        dictionary contains start campaign config message
    """

    campaign_list = ''
    campaigns = get_user_campaigns(event['user']['email'])
    for i in range(len(campaigns)):
        campaign = convert_entity_to_campaign(campaigns[i])
        campaign_list = campaign_list + '<b>{}.</b> {}<br>'.format(i + 1, campaign.name)

    return {
              "actionResponse": {
                "type": "UPDATE_MESSAGE"
              },
              "cards": [
                {
                  "header": build_header('Editing'),
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": "Please send a number corresponding to the campaign " +
                              "you would like to edit.<br>" + campaign_list
                          }
                        }
                      ]
                    },
                    {
                      "widgets": [
                        {
                          "buttons": [
                            {
                              "textButton": {
                                "text": "QUIT",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "quit_campaign",
                                  }
                                }
                              }
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }

def start_user_campaign(event):
    """Formats the introduction message response
    Args:
      None
    Returns:
      dict
        dictionary contains start campaign config message
    """

    return {
              "actionResponse": {
                "type": "UPDATE_MESSAGE"
              },
              "cards": [
                {
                  "header": build_header('Editing'),
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": "You will now start configuring your " +
                                "DSA Campaign! If you would like to " + 
                                "save and quit, you may click <b>\"QUIT\" " +
                                "</b>below at any point in the " +
                                "configuration process. Additionally, " + 
                                "the process of entering data is simple. " + 
                                "<br><b>1.</b> I will prompt you for a specific " +
                                "parameter - you must enter in a valid " +
                                "entry before moving on. <br><b>2.</b> Next, I " +
                                "will ask for your confirmation. " +
                                "<br><b>3.</b> You will be able to review and edit your " +
                                "settings before final submission. " +
                                "<br>To begin, please click <b>CONTINUE</b>."
                          }
                        }
                      ]
                    },
                    {
                      "widgets": [
                        {
                          "buttons": [
                            {
                              "textButton": {
                                "text": "CONTINUE",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "continue_campaign",
                                  }
                                }
                              }
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }

def create_submission_message(event):
    """Formats the introduction message response
    Args:
      None
    Returns:
      dict
        dictionary contains start campaign config message
    """

    return {
              "actionResponse": {
                "type": "UPDATE_MESSAGE"
              },
              "cards": [
                {
                  "header": build_header('Editing'),
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": "Congratulations on submitting your campaign estimate, {}! ".format(event['user']['displayName']) +
                            "Your Dynamic Search Ad Campaign " +
                            "estimate will be available shortly!"
                          }
                        }
                      ]
                    },
                    {
                      "widgets": [
                        {
                          "buttons": [
                            {
                              "textButton": {
                                "text": "QUIT",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "quit_campaign",
                                  }
                                }
                              }
                            },
                            {
                              "textButton": {
                                "text": "VISIT WEBSITE",
                                "onClick": {
                                  "openLink": {
                                      "url": "https://dsa-uplift-estimation-2020.uc.r.appspot.com/"
                                  }
                                }
                              }
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }

def create_configure_message(user_id, phase_num, editing):
    """Determine what setting message to provide based on
      phase number.
    Args:
      setting: the name of the setting to be input.
      campaignName: the name of the campaign being configured
    Returns:
      dictionary containing configuration prompt message for
      specified phase number
    """
    
    if phase_num == PHASE_NUM.KEYWORD_CAMPAIGN:
        return create_keyword_campaign_list(user_id, editing, False)
    configure_str = '{}'.format(PHASE_DICTIONARY.get(phase_num)[PROMPT_MSG_INDEX])

    return {
              "actionResponse": {
                "type": "UPDATE_MESSAGE" if not editing else "NEW_MESSAGE"
              },
              "cards": [
                {
                  "header": build_header('Editing'),
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": configure_str
                          }
                        }
                      ]
                    },
                    {
                      "widgets": [
                        {
                          "buttons": add_configure_buttons(editing)
                        }
                      ]
                    }
                  ]
                }
              ]
            }

def add_configure_buttons(editing):
    """Returns a list of buttons to be used by
    create_configure_message, adds additional
    'BACK' button if user is editing a submission
    Args:
      editing: boolean for if user is editing
    Returns:
      list
        containing the buttons to be used by the
        create_configure_message function
    """
    
    button_list = [
      {
        "textButton": {
          "text": "QUIT",
          "onClick": {
            "action": {
              "actionMethodName": "quit_campaign",
            }
          }
        }
      }   
    ]

    if editing:
        button_list.append(
          {
            "textButton": {
              "text": "BACK",
              "onClick": {
                "action": {
                  "actionMethodName": "back_submission",
                }
              }
            }
          }
        )

    return button_list  
        

def create_join_message(event):
    """Create a join message with content based on event data
    Args:
      event: A dictionary with the event data.
    Returns:
      dict
        dictionary containing join response message
    """

    return {
              "cards": [
                {
                  "header": build_header('Standby'),
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": "Thanks for adding the Dynamic Search Ads Configuration Bot, {}! To begin, please choose what you would like to do below.".format(event['user']['displayName'])
                          }
                        },
                      ]
                    },
                    {
                      "widgets": [
                        {
                          "buttons": add_edit_button(event['user']['email'])
                        }
                      ]
                    }
                  ]
                }
              ]
            }

def create_home_message(event):
    """Create a home message with content based on event data.
    User is prompted with this when configuration process is
    cancelled.
    Args:
      event: A dictionary with the event data.
    Returns:
      dict
        dictionary containing home response message
    """

    return {
              "actionResponse": {
                "type": "UPDATE_MESSAGE"
              },
              "cards": [
                {
                  "header": build_header('Standby'),
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": "Please choose what you would like to do below."
                          }
                        },
                      ]
                    },
                    {
                      "widgets": [
                        {
                          "buttons": add_edit_button(event['user']['email'])
                        }
                      ]
                    }
                  ]
                }
              ]
            }

def add_edit_button(user_id):
    """Determines if home page requires an edit button
    Args:
      user_id:
        user_id being used to request home page
    Returns:
      list
        list of dictionaries containing buttons
    """
    
    button_list = [
      {
        "textButton": {
          "text": "START NEW CAMPAIGN",
          "onClick": {
            "action": {
              "actionMethodName": "start_campaign",
            }
          }
        }
      }
    ]
    if (len(get_user_campaigns(user_id)) != 0):
      button_list.append(
        {
          "textButton": {
            "text": "EDIT EXISTING CAMPAIGN",
            "onClick": {
              "action": {
                "actionMethodName": "edit_campaign",
              }
            }
          }
        }
      )
    
    keyword_json = get_keyword_campaigns(user_id)
    
    if (len(keyword_json) != 0):
        button_list.append(
          {
            "textButton": {
              "text": "VIEW CAMPAIGNS",
              "onClick": {
                "action": {
                  "actionMethodName": "view_campaigns",
                }
              }
            }
          }
        )

    return button_list

def get_campaign_overview(campaign_data, viewing):
    """Returns a campaign overview for users to view,
    Buttons on message vary based on submission boolean

    Args:
      campaign_data:
        CampaignData used to populate campaign data
      viewing:
        bool representing if user is viewing a campaign from campaign list prompt, (determines if this updates previous message)
    Returns:
      dict
        dictionary containing overview message
    """    

    not_set = 'None'
    return {
              "actionResponse": {
                "type": "UPDATE_MESSAGE" if not viewing else "NEW_MESSAGE"
              },
              "cards": [
                {
                  "header": build_header(campaign_data.status.capitalize()),
                  "sections": [
                    {
                      "widgets": [
                        {
                          "keyValue": {
                            "topLabel": "Keyword Campaign ID",
                            "content": campaign_data.keyword_campaign_id if campaign_data.keyword_campaign_id != '' else not_set,
                            "icon": "STAR"
                          }
                        },
                        {
                          "keyValue": {
                            "topLabel": "Campaign Name",
                            "content": campaign_data.name if campaign_data.name != '' else not_set,
                            "icon": "STAR"
                          }
                        },
                        {
                          "keyValue": {
                            "topLabel": "Start Date",
                            "content": campaign_data.start_date if campaign_data.start_date != '' else not_set,
                            "icon": "INVITE"
                          }
                        },
                        {
                          "keyValue": {
                            "topLabel": "End Date",
                            "content": campaign_data.end_date if campaign_data.end_date != '' else not_set,
                            "icon": "INVITE"
                          }
                        },
                        {
                          "keyValue": {
                            "topLabel": "Daily Budget",
                            "content": '${}'.format(campaign_data.daily_budget) if campaign_data.daily_budget != 0.0 else not_set,
                            "icon": "DOLLAR"
                          }
                        },
                        {
                          "keyValue": {
                            "topLabel": "Cost Per Click",
                            "content": '${}'.format(campaign_data.manual_CPC) if campaign_data.manual_CPC != 0.0 else not_set,
                            "icon": "DOLLAR"
                          }
                        },
                        {
                          "keyValue": {
                            "topLabel": "Domain",
                            "content": campaign_data.domain if campaign_data.domain != '' else not_set,
                            "icon": "BOOKMARK"
                          }
                        },
                        {
                          "keyValue": {
                            "topLabel": "Target Pages",
                            "content": campaign_data.targets if campaign_data.targets != '' else not_set,
                            "icon": "BOOKMARK"
                          }
                        },
                        {
                          "keyValue": {
                            "topLabel": "Locations",
                            "content": campaign_data.locations if campaign_data.locations != '' else not_set,
                            "icon": "MAP_PIN"
                          }
                        },
                        {
                          "keyValue": {
                            "topLabel": "Negative Locations",
                            "content": campaign_data.neg_locations if campaign_data.neg_locations != '' else not_set,
                            "icon": "MAP_PIN"
                          }
                        },
                        {
                          "keyValue": {
                            "topLabel": "Ad Text",
                            "content": campaign_data.ad_text if campaign_data.ad_text != '' else not_set,
                            "icon": "DESCRIPTION"
                          }
                        }
                      ]
                    },
                    {
                      "widgets": [
                        {
                          "keyValue": {
                            "topLabel": "Impressions",
                            "content": campaign_data.impressions if campaign_data.impressions != '' else not_set,
                            "icon": "PERSON"
                          }
                        },
                        {
                          "keyValue": {
                            "topLabel": "Clicks",
                            "content": campaign_data.clicks if campaign_data.clicks != '' else not_set,
                            "icon": "CONFIRMATION_NUMBER_ICON"
                          }
                        },
                        {
                          "keyValue": {
                            "topLabel": "Cost",
                            "content": '${}'.format(campaign_data.cost) if campaign_data.cost != '' else not_set,
                            "icon": "DOLLAR"
                          }
                        }
                      ]
                    },
                    {
                      "widgets": [
                        {
                          "buttons": [
                            {
                              "textButton": {
                                "text": "BACK",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "quit_campaign"
                                  }
                                }
                              }
                            },
                            {
                              "textButton": {
                                "text": "DELETE CAMPAIGN",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "delete_campaign",
                                    "parameters": [
                                      {
                                        "key": "campaign_id",
                                        "value": campaign_data.campaign_id
                                      },
                                      {
                                        "key": "keyword_campaign_id",
                                        "value": campaign_data.keyword_campaign_id
                                      }
                                    ]
                                  }
                                }
                              }
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }

def create_campaign_deletion_confirmation(error):
    message = 'Campaign successfully deleted.'
    if error:
        message = 'Campaign cannot be deleted right now, please try again.'

    return {
              "actionResponse": {
                "type": "UPDATE_MESSAGE"
              },
              "cards": [
                {
                  "header": build_header('Alert'),
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": message
                          }
                        }
                      ]
                    },
                    {
                      "widgets": [
                        {
                          "buttons": [
                            {
                              "textButton": {
                                "text": "QUIT",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "quit_campaign",
                                  }
                                }
                              }
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }


def build_header(subtitle):
    """Create a header dictionary to be used by every message
    Args:
      subtitle: User defined subtitle appearing on the messages
    Returns:
      dict
        dictionary containing the header information for a message
    """

    header_dict = {
      'title': 'Dynamic Search Ads Configuration Bot',
      'subtitle': subtitle,
      'imageStyle': "AVATAR",
      'imageUrl': "https://9to5google.com/2018/05/05/exclusive-new-google-app-icons-i-o-2018-gallery/ads_512dp/"
    }

    return header_dict

def confirm_campaign_delete(user_id, campaign_id, keyword_campaign_id):

    campaigns = get_dsa_campaigns(user_id, keyword_campaign_id)
    campaign_to_delete = None
    for campaign in campaigns:
        if campaign.campaign_id == campaign_id:
            campaign_to_delete = campaign
            break

    return {
              "cards": [
                {
                  "header": build_header('WARNING'),
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": "<font color=\"#ff0000\">ALERT:</font> Are you sure you would like to delete the campaign, <b>{}</b>?".format(campaign_to_delete.name)
                          }
                        }
                      ]
                    },
                    {
                      "widgets": [
                        {
                          "buttons": [
                            {
                              "textButton": {
                                "text": "NO",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "no_action",
                                    "parameters": [
                                      {
                                        "key": "campaign_id",
                                        "value": campaign_id
                                      }
                                    ]
                                  }
                                }
                              }
                            },
                            {
                              "textButton": {
                                "text": "YES",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "yes_action",
                                    "parameters": [
                                      {
                                        "key": "campaign_id",
                                        "value": campaign_id
                                      }
                                    ]
                                  }
                                }
                              }
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }

