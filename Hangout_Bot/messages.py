from constant import *

# phase number (key) : [phase name, phase specific prompt message]
phase_dictionary = {
  0: ['name',
      'Please input the name that will be associated with your dynamic search ad campaign' +
      ' estimate.'],
  1: ['start date',
      'Please input the date that your campaign estimate starts. <b>Must be in the form ' +
      'mm-dd-yyyy.</b>'],
  2: ['end date',
      'Please input the date that your campaign estimate will end. <b>Must be in the form ' +
      'mm-dd-yyyy.</b>'],
  3: ['daily budget', 'Please input the amount you are willing to on the campaign spend each day.'],
  4: ['locations', 'Please enter a comma separated list of state codes that ' +
      'your ad will target. Must be in the form of a valid US State ' +
      'abbreviation (Ex: \'CA, TX, MN\') (Must follow this form!). Enter \'USA\'' +
      'if you would like to target the entire US.'],
  5: ['negative locations', 'Are there any locations that your ad will not target? ' +
      'Ex: CA, USA (Must follow this format!). Input N/A if not applcable.'],
  6: ['domain', 'What is the domain of the webpage being advertised? ' +
      '(Ex: http://google.com)'],
  7: ['targets', 'What are specific target pages of your domain that you would like to ' +
      'specifically advertise? Multiple target pages can be input as a comma separated list. ' +
      '(Ex: http://google.com/somepage, http://google.com/anotherpage)'],
  8: ['cost per click', 'What is the desired click per click? ' +
      'The amount specified here will be how much you pay everytime an ad is clicked.'],
  9: ['ad text', 'Text that will appear on your ad.']
}

# [Error Handling]
def error_handler(message, phase_num):
    """Error handle the message parameter based on the phase number parameter.
    Args:
      message: The string requiring validation
      phase_num: the phase number that will be as reference to validate the message
    Returns:
      True: if message is valid
      String: error message if string is not valid.
    """

    # error map (phase_dictionary key) : error handling reference
    error_dictionary = {
      0: '',  # name cannot be an empty string or length 0
      1: 'mm-dd-yyyy',   # start date must be in the form mm-dd-yyyy
      2: 'mm-dd-yyyy',   # end date must be in the form mm-dd-yyyy
      3: 0.01,   # daily budget must be at least 1 cent.
      # 6/7 will be validated using python URL validator
      8: 0.01   # manual CPC must be at least 1 cent.
    }

    # allows each success message to be changed at once, if necessary
    success = True

    # phase 0: name
    if phase_num == 0:
        return success if (len(message) > 0 and message != error_dictionary[0]) else 'Name is not valid!'

    # phase 1: start date, phase 2: end date
    elif phase_num == 1 or phase_num == 2:
        date_parameters = message.split('-')
        if len(date_parameters) == 3:
            if int(date_parameters[0]) > 0 and int(date_parameters[0]) <= 12:
                if (int(date_parameters[1]) > 0 and int(date_parameters[1]) <= 30) or (int(date_parameters[0]) % 2 == 0 and int(date_parameters[1]) == 31):
                    if (int(date_parameters[2]) >= 2000 and int(date_parameters[2]) <= 2099):
                        return success
                    else:
                        return 'Year can only be between 2000 and 2099!'
                else:
                    return 'Day must be between 1 and 31! If you entered 31 as a day, make sure the month has 31 days!'
            else:
                return 'Month must be between 1 and 12!'
        return 'Date is not in the right format! Must be in the form mm-dd-yyyy'

    # phase 3: daily budget, phase 8: manual CPC
    elif phase_num == 3 or phase_num == 8:
        attribute_name = 'Daily budget' if phase_num == 3 else 'Manual CPC (Cost Per Click)'
        return success if Decimal(message) >= error_dictionary[3] else '{} must be at least 0.01!'.format(attribute_name)
    
    # phase 4: locations, phase 5: negative locations
    elif phase_num == 4 or phase_num == 5:
        locations = message.split(',')
        for idx in len(locations):
            # trim white space
            locations[idx] = locations[idx].strip()

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
    elif phase_num == 6 or phase_num == 7:
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

        # if phase number is 7, then message is a comma separated list
        if (phase_num == 7):
            domains = message.split(',')
            for idx in range(len(domains)):
                # trim white space
                domains[idx] = domains[idx].strip()
                if not re.match(url_regex, domains[idx]):
                    return 'URL is not valid, please ensure that you specified the correct {}!'.format(attribute_name)
            return success
        
        return success if re.match(url_regex, message) else 'URL is not valid, please ensure that you specified the correct {}!'.format(attribute_name)

def error_message(error_msg, phase_num):
    error = "<font color=\"#ff0000\">ERROR</font>:<b> {}</b><br><b>Please send a valid value for your campaign {}!</b>".format(error_msg, phase_dictionary.get(phase_num)[PHASE_NAME_INDEX]) if phase_num != -1 else "<font color=\"#ff0000\">ERROR</font>:<b>{}</b>".format(error_msg)
    return {
              "cards": [
                {
                  "header": {
                    "title": "Dynamic Search Ads Configuration Bot",
                    "subtitle": "Alert",
                    "imageStyle": "AVATAR",
                    "imageUrl": "https://9to5google.com/2018/05/05/exclusive-new-google-app-icons-i-o-2018-gallery/ads_512dp/"
                  },
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": error
                          }
                        }
                      ]
                    }
                  ]
                }
              ]
            }


# [Message Generating Functions]
def create_confirmation_message(event, phase_num):
    user_key = obtain_user_key(event['user']['email'])

    return {
              "cards": [
                {
                  "header": {
                    "title": "Dynamic Search Ads Configuration Bot",
                    "subtitle": "Editing",
                    "imageStyle": "AVATAR",
                    "imageUrl": "https://9to5google.com/2018/05/05/exclusive-new-google-app-icons-i-o-2018-gallery/ads_512dp/"
                  },
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": "You picked <b>\'{}\'</b> for your campaign {}, is this correct?".format(event['message']['text'], phase_dictionary.get(phase_num)[PHASE_NAME_INDEX])
                          }
                        }
                      ]
                    },
                    {
                      "widgets":[
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
                                        "key": "phase_num",
                                        "value": phase_num
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

def start_user_campaign(event):
    """Start campaign configuration process with event parameter
    Args:
      event: A dictionary with the event data.
    """
    return {
              "actionResponse": {
                "type": "UPDATE_MESSAGE"
              },
              "cards": [
                {
                  "header": {
                    "title": "Dynamic Search Ads Configuration Bot",
                    "subtitle": "Editing",
                    "imageStyle": "AVATAR",
                    "imageUrl": "https://9to5google.com/2018/05/05/exclusive-new-google-app-icons-i-o-2018-gallery/ads_512dp/"
                  },
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": "You will now start configuring your " +
                                "DSA Campaign! If you would like to " + 
                                "save and quit, you may click \'QUIT " +
                                "CAMPAIGN\' below at any point in the " +
                                "configuration process. Additionally, " + 
                                "the process of entering data is simple. " + 
                                "<br><b>1.</b> I will prompt you for a specific " +
                                "parameter - you must enter in a valid " +
                                "entry before moving on. <br><b>2.</b> Next, I " +
                                "will ask for your confirmation. " +
                                "<br><b>3.</b> You will be able to review and edit your " +
                                "settings before final submission. " +
                                "<br>To begin, <b>please enter a valid name " +
                                "for your campaign.</b>"
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
                                "text": "QUIT CAMPAIGN",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "cancel_campaign",
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


def create_configure_message(phase_num):
    """Determine what setting message to provide based on
      phase number.
    Args:
      setting: the name of the setting to be input.
      campaignName: the name of the campaign being configured
    Returns:
      dictionary containing configuration prompt message for
      specified phase number
    """

    return {
              "actionResponse": {
                "type": "UPDATE_MESSAGE"
              },
              "cards": [
                {
                  "header": {
                    "title": "Dynamic Search Ads Configuration Bot",
                    "subtitle": "Editing",
                    "imageStyle": "AVATAR",
                    "imageUrl": "https://9to5google.com/2018/05/05/exclusive-new-google-app-icons-i-o-2018-gallery/ads_512dp/"
                  },
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": '<br>{}</b>'.format(phase_dictionary.get(phase_num)[PROMPT_MSG_INDEX])
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
                                "text": "CANCEL CAMPAIGN",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "cancel_campaign",
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

def create_join_message(event):
    """Create a join message with content based on event data
    Args:
      event: A dictionary with the event data.
    """
    return {
              "cards": [
                {
                  "header": {
                    "title": "Dynamic Search Ads Configuration Bot",
                    "subtitle": "Standby",
                    "imageStyle": "AVATAR",
                    "imageUrl": "https://9to5google.com/2018/05/05/exclusive-new-google-app-icons-i-o-2018-gallery/ads_512dp/"
                  },
                  "sections": [
                    {
                      "widgets": [
                        {
                          "textParagraph": {
                            "text": "Thanks for adding the Dynamic Search Ads Configuration Bot, {}! To begin, please choose if you would like to start a new campaign or edit an existing campaign.".format(event['user']['displayName'])
                          }
                        },
                      ]
                    },
                    {
                      "widgets":[
                        {
                          "buttons": [
                            {
                              "textButton": {
                                "text": "START NEW CAMPAIGN",
                                "onClick": {
                                  "action": {
                                    "actionMethodName": "start_campaign",
                                  }
                                }
                              }
                            },
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
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }