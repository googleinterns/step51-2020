# Copyright 2017 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the 'License');
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an 'AS IS' BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# pylint: disable=invalid-name
"""
Hangouts Chat bot that responds to events and messages from a room asynchronously.
"""

# [START bot processing]

import re
import logging
from google.cloud import datastore
from flask import Flask, render_template, request, json
from decimal import Decimal

app = Flask(__name__)

# [START constants - temporary (will move to dedicated file)]
PHASE_NUM_INDEX = 0
CAMPAIGN_NAME_INDEX = 1

PHASE_NAME_INDEX = 0
PROMPT_MSG_INDEX = 1

# phase number (key) : [phase name, phase specific prompt message]
phase_dictionary = {
  0: ['name',
      'Please input the name that will be associated with your dynamic search ad campaign' +
      ' estimate.'],
  1: ['startDate',
      'Please input the date that your campaign estimate starts. Must be in the form ' +
      'mm-dd-yyyy.'],
  2: ['endDate',
      'Please input the date that your campaign estimate will end. Must be in the form ' +
      'mm-dd-yyyy.'],
  3: ['dailyBudget', 'Please input the amount you are willing to on the campaign spend each day.'],
  4: ['locations', 'What locations locations that your ad will target? Must be in the form of a valid US State abbreviation (CA, TX, MN, etc.) and USA. ' +
      'Ex: CA, USA (Must follow this form!)'],
  5: ['negativeLocations', 'Are there any locations that your ad will not target? ' +
      'Ex: CA, USA (Must follow this format!). Input N/A if not applcable.'],
  6: ['domain', 'What is the domain of the webpage being advertised? ' +
      '(Ex: http://google.com)'],
  7: ['targets', 'What are specific target pages of your domain that you would like to ' +
      'specifically advertise? Multiple target pages can be input as a comma separated list. ' +
      '(Ex: http://google.com/somepage, http://google.com/anotherpage)'],
  8: ['manualCPC', 'What is the desired click per click? ' +
      'The amount specified here will be how much you pay everytime an ad is clicked.']
}

# dictionary containg active users, each key refers to [phase_num, campaign_name], helps reduce API calls
active_users = {}


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

    success = True

    # phase 0: name
    if phase_num == 0:
        return success if (len(message) > 0 and message != error_dictionary[0]) else 'Name is not valid!'
    
    # phase 1: start date
    elif phase_num == 1:
        date_parameters = message.split('-')
        error_message = ''
        if len(date_parameters) == 3:
            if int(date_parameters[0]) > 0 and int(date_parameters[0]) <= 12:
                if (int(date_parameters[1]) > 0 and int(date_parameters[1]) <= 30) or (int(date_parameters[0]) % 2 == 0 and int(date_parameters[1]) == 31):
                    if (int(date_parameters[2]) >= 2000 and int(date_parameters[2]) <= 2099):
                        return success
                    else:
                        error_message = 'Year can only be between 2000 and 2099!'
                else:
                    error_message = 'Day must be between 1 and 31! If you entered 31 as a day, make sure the month has 31 days!'
            else:
                error_message = 'Month must be between 1 and 12!'
        else:
          error_message = 'Date is not in the right format! Must be in the form mm-dd-yyyy'
        return error_message

    # phase 2: end date
    elif phase_num == 2:
        date_parameters = message.split('-')
        error_message = ''
        if (len(date_parameters) == 3):
            if (int(date_parameters[0]) > 0 and int(date_parameters[0]) <= 12):
                if ((int(date_parameters[1]) > 0 and int(date_parameters[1]) <= 30) or (int(date_parameters[0]) % 2 == 0 and int(date_parameters[1]) == 31)):
                    if (int(date_parameters[2]) >= 2000 and int(date_parameters[2]) <= 2099):
                        return success
                    else:
                        error_message = 'Year can only be between 2000 and 2099!'
                else:
                    error_message = 'Day must be between 1 and 31! If you entered 31 as a day, make sure the month has 31 days!'
            else:
                error_message = 'Month must be between 1 and 12!'
        else:
          error_message = 'Date is not in the right format! Must be in the form mm-dd-yyyy'
        return error_message
    
    # phase 3: daily budget, phase 8: manual CPC
    elif phase_num == 3 or phase_num == 8:
        attribute_name = 'Daily budget' if phase_num == 3 else 'Manual CPC (Cost Per Click)'
        return success if Decimal(message) >= error_dictionary[3] else '{} must be at least 0.01!'.format(attribute_name)
    
    # phase 4: locations, phase 5: negative locations
    elif phase_num == 4 or phase_num == 5:
        if len(message) == 2:
            regex = re.compile('[A-Za-z][A-Za-z]')
            return success if re.match(regex, message) else 'State code must only contain 2 letters! Ex: CA, TX, MN, PA'
        else:
            return 'State code is not valid, Ex: CA, TX, MN, PA'
    
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
            message = message.split(',')
            for target_page in message:
                if not re.match(url_regex, target_page):
                    return 'URL is not valid, please ensure that you specified the correct {}!'.format(attribute_name)
            return success
        
        return success if re.match(url_regex, message) else 'URL is not valid, please ensure that you specified the correct {}!'.format(attribute_name)


def handle_message(event):
    """Determine what logic to execute based upon message event data.
    Args:
      event: A dictionary with the event data.
    """
    user_id = event['user']['email']
    error_msg = error_handler(event['message']['text'], active_users.get(user_id)[PHASE_NUM_INDEX])

    if (error_msg is not True):
        return error_message(error_msg, active_users.get(user_id)[PHASE_NAME_INDEX])

    if (active_users.get(user_id)[PHASE_NUM_INDEX] == 0):
        # set active user campaign name and append phase number that user is on
        active_users[user_id] = [active_users.get(user_id)[PHASE_NUM_INDEX] + 1, event['message']['text']]
    else:
        # append specific active user phase number
        active_users[user_id][PHASE_NUM_INDEX] = active_users.get(user_id)[PHASE_NUM_INDEX] + 1

    return create_confirmation_message(event, active_users.get(user_id)[PHASE_NUM_INDEX])

def error_message(error_msg, phase_num):
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
                            "text": "<font color=\"#ff0000\">ERROR</font>:<br>{}<br>Please send a valid value for your campaign {}!".format(error_msg, phase_dictionary.get(phase_num)[PHASE_NAME_INDEX])
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

def create_confirmation_message(event, phase_num):
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
                            "text": "You picked {} for your campaign {}, is this correct?".format(event['message']['text'], phase_dictionary.get(phase_num)[PHASE_NAME_INDEX])
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
    user_id = event['user']['email']

    if (active_users.get(user_id) is not None):
        print('error')
        # add_wip_campaign(user_id, active_users.get(user_id)[PHASE_NUM_INDEX], active_users.get(user_id)[CAMPAIGN_NAME_INDEX])

    active_users[user_id] = [0, None]

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
                                "To begin, <b>please enter a name for your " +
                                "campaign.</b>"
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

def obtain_user_key(event):
    """Determine what key to provide based upon event data.
    Args:
      event: A dictionary with the event data.
    """
    return datastore.Client().key('UserData', event['user']['email'])

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


def create_configure_message(phase_num):
    """Determine what setting message to provide based on
      phase number.
    Args:
      setting: the name of the setting to be input.
      campaignName: the name of the campaign being configured
    """

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
                            "text": phase_dictionary.get(phase_num)[PROMPT_MSG_INDEX]
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

def handle_button_click(event):
    """Determine what logic to execute based upon button event data.
    Args:
      event: A dictionary with the event data.
    """
    event_action = event['action']['actionMethodName']
    curr_phase_num = active_users.get(event['user']['email'])[PHASE_NUM_INDEX]

    if event_action == 'start_campaign':
        return start_user_campaign(event)
    elif event_action == 'edit_campaign':
        # return edit_user_campaign(event)
        print('editing')
    elif event_action == 'cancel_campaign':
        print('cancelling')
    elif event_action == 'yes_action':
        active_users[event['user']['email']] = [curr_phase_num + 1, active_users.get(event['user']['email'])[CAMPAIGN_NAME_INDEX]]
        return create_configure_message(curr_phase_num + 1)
    elif event_action == 'no_action':
        return create_configure_message(curr_phase_num)


def format_response(event):
    """Determine what response to provide based upon event data.
    Args:
      event: A dictionary with the event data.
    Returns:
      responseText: dictionary containing bot response
    """
    event_type = event['type']

    responseText = None

    # Case: The bot was added to a DM
    if event_type == 'ADDED_TO_SPACE' and event['space']['type'] == 'DM':
        responseText = create_join_message(event)
    elif event_type == 'MESSAGE':
        responseText = handle_message(event)
    elif event_type == 'CARD_CLICKED':
        responseText = handle_button_click(event)
    else:
        responseText = {"text": "UNKNOWN EVENT ENCOUNTERED"}

    # The following three lines of code update the thread that raised the event.
    # Delete them if you want to send the message in a new thread.
    if event_type == 'MESSAGE' and event['message']['thread'] is not None:
        thread_id = event['message']['thread']
        responseText['thread'] = thread_id

    return responseText

# [END bot processing]


@app.route('/', methods=['GET'])
def home_get():
    """Respond to GET requests to this endpoint.
    This function responds to requests with a simple HTML landing page for this
    App Engine instance.
    """

    return render_template('home.html')


if __name__ == '__main__':
    # This is used when running locally. Gunicorn is used to run the
    # application on Google App Engine. See entrypoint in app.yaml.

    app.run(host='127.0.0.1', port=8080, debug=True)


@app.route('/', methods=['POST'])
def home_post():
    """Respond to POST requests to this endpoint.
    All requests sent to this endpoint from Hangouts Chat are POST
    requests.
    """

    data = request.get_json()

    resp = None

    if data['type'] == 'REMOVED_FROM_SPACE':
        logging.info('Bot removed from a space')

    else:
        resp_dict = format_response(data)
        resp = json.jsonify(resp_dict)

    return resp

# [START CampaignData class - temporary (will move to separate file)]

class CampaignData:
    def __init__(self):
      self.owner = ''
      self.name = ''
      self.start_date = ''
      self.end_data = ''
      self.daily_budget = 0.0
      self.manual_CPC = 0.0
      self.locations = ''
      self.neg_locations = ''
      self.domain = ''
      self.targets = ''

    def __init__(self, owner):
      self.owner = owner
      self.name = ''
      self.start_date = ''
      self.end_data = ''
      self.daily_budget = 0.0
      self.manual_CPC = 0.0
      self.locations = ''
      self.neg_locations = ''
      self.domain = ''
      self.targets = ''

    def __init__(self, name, start_date, end_date, daily_budget, manual_CPC, locations, neg_locations, domain, targets):
        self.owner = ''
        self.name = name
        self.start_date = start_date
        self.end_data = end_date
        self.daily_budget = daily_budget
        self.manual_CPC = manual_CPC
        self.locations = locations
        self.neg_locations = neg_locations
        self.domain = domain
        self.targets = targets

    def set_owner(self, owner):
        self.owner = owner

    def set_name(self, name):
        self.name = name


    def set_start_date(self, start_date):
        self.start_date = start_date


    def set_end_date(self, end_date):
        self.end_date = end_date


    def set_daily_budget(self, daily_budget):
        self.daily_budget = daily_budget


    def set_manual_CPC(self, manual_CPC):
        self.manual_CPC = manual_CPC


    def set_locations(self, locations):
        self.locations = locations


    def set_neg_locations(self, neg_locations):
        self.neg_locations = neg_locations


    def set_domain(self, domain):
        self.domain = domain


    def set_targets(self, targets):
        self.targets = targets
