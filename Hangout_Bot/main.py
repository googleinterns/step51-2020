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
from os import sys, path 
# add current directory as directory to search
sys.path.append(".")

import re
import logging
from google.cloud import datastore
from flask import Flask, render_template, request, json
from decimal import Decimal
from campaigndata import CampaignData
from constant import *
from handlers import *

app = Flask(__name__)

# [Main Event Handler]

def format_response(event):
    """Determine what response to provide based upon type of event
       and event data
    Args:
      event: A dictionary with the event data.
    Returns:
      responseText: dictionary containing bot response
    """
    event_type = event['type']

    responseText = None

    # Case: The bot was added to a DM
    if event_type == 'ADDED_TO_SPACE' and event['space']['type'] == 'DM':
        responseText = handle_join(event)
    elif event_type == 'MESSAGE':
        responseText = handle_message(event)
    elif event_type == 'CARD_CLICKED':
        responseText = handle_button_click(event)
    elif event_type == "REMOVED_FROM_SPACE":
        # TODO: add user progress to datastore
        # user is no longer active, remove from active users
        # TODO: find alternative to del active_users[event['users']['email']]
        print('REMOVED FROM SPACE')
    else:
        responseText = {"text": "UNKNOWN EVENT ENCOUNTERED"}

    # The following three lines of code update the thread that raised the event.
    # Delete them if you want to send the message in a new thread.
    if event_type == 'MESSAGE' and event['message']['thread'] is not None:
        thread_id = event['message']['thread']
        responseText['thread'] = thread_id

    return responseText

# [END bot processing]

# [Hangouts Bot API Functions]

@app.route('/', methods=['GET'])
def home_get():
    """Respond to GET requests to this endpoint.
    This function responds to requests with a simple HTML landing page for this
    App Engine instance.
    """

    # TODO: add a valid html file
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