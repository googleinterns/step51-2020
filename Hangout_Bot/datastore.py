from google.cloud import datastore
from campaigndata import CampaignData
from userdata import *

# [Datastore Interaction Functions]
def obtain_user_key(user_id):
    """Determine what key to provide based upon event data.
    Args:
      event: A dictionary with the event data.
    """
    return datastore.Client().key('UserData', user_id)


def get_campaign_key(event, campaign_name):
    return datastore.Client().key('CampaignData', '{}{}'.format(event['user']['email'], campaign_name))


def update_campaign_data(campaign_data, phase_num, value):
    if (phase_num == 1):
        campaign_data.set_start_date(value)
    elif (phase_num == 2):
        campaign_data.set_end_date(value)
    elif (phase_num == 3):
        campaign_data.set_daily_budget(value)
    elif (phase_num == 4):
        campaign_data.set_locations(value)
    elif (phase_num == 5):
        campaign_data.set_neg_locations(value)
    elif (phase_num == 6):
        campaign_data.set_domain(value)
    elif (phase_num == 7):
        campaign_data.set_targets(value)
    elif (phase_num == 8):
        campaign_data.set_manual_CPC(value)   
    elif (phase_num == 9):
        campaign_data.set_ad_text(value) 
    return campaign_data


def update_campaign_data(campaign_key, campaign_data):
    datastore_client = datastore.Client()
    campaign_entity = datastore_client.get(campaign_key)
    campaign_entity.update({'campaign_data': campaign_data})
    datastore_client.put(campaign_entity)

def add_user(event):
    user_key = obtain_user_key(event['user']['email'])
    entity = datastore.Entity(user_key)
    entity.update({
      'user': event['user']['email'],
      'phase_num': -1,
      'accepting_text': True,
      'campaign_name': ''
    })
    datastore.Client().put(entity)

def get_user_data(user_key):

    user_entity = datastore.Client().get(user_key)
    return user_entity

def get_campaign_data(campaign_key):
    return convert_entity_to_campaign(datastore.Client().get(campaign_key))

# [Datastore and class conversion functions]

def convert_entity_to_campaign(campaign_entity):
    # Initialize the campaign object
    dsa_campaign = CampaignData(campaign_entity['owner'])

    # individually set each parameter with the entity values
    dsa_campaign.set_name(campaign_entity['name'])
    dsa_campaign.set_start_date(campaign_entity['start_date'])
    dsa_campaign.set_end_date(campaign_entity['end_date'])
    dsa_campaign.set_daily_budget(campaign_entity['daily_budget'])
    dsa_campaign.set_manual_CPC(campaign_entity['manual_CPC'])
    dsa_campaign.set_locations(campaign_entity['locations'])
    dsa_campaign.set_neg_locations(campaign_entity['neg_locations'])
    dsa_campaign.set_domain(campaign_entity['domain'])
    dsa_campaign.set_targets(campaign_entity['targets'])
    dsa_campaign.set_ad_text(campaign_entity['ad_text'])

    return dsa_campaign

def convert_campaign_to_entity(campaign):
    # Create the entity key
    entity_key = datastore.Client().key('CampaignData', '{}{}'.format(event['user']['email'], campaign_name))

    # Create the entity
    entity = datastore.Entity(entity_key)

    # Populate the entity parameters with campaign values
    entity['owner'] = campaign.owner
    entity['name'] = campaign.name
    entity['start_date'] = campaign.start_date
    entity['end_date'] = campaign.end_date
    entity['daily_budget'] = campaign.daily_budget
    entity['manual_CPC'] = campaign.manual_CPC
    entity['locations'] = campaign.locations
    entity['neg_locations'] = campaign.neg_locations
    entity['domain'] = campaign.domain
    entity['targets'] = campaign.targets
    entity['ad_text'] = campaign.ad_text

    # Returns the created entity
    return entity

def convert_entity_to_user(entity):
    user_data = UserData(entity['user'])

    if (entity['accepting_text'] == 'true'):
        user_data.set_accepting_text(True)
    else:
        user_data.set_accepting_text(False)
    
    user_data.set_campaign_name(entity['campaign_name'])
    user_data.set_phase_num(entity['phase_num'])

    return user_data

def convert_user_to_entity(user_data):
    entity_key = obtain_user_key(user_data.user_id)

    entity = datastore.Entity(entity_key)

    entity['user'] = user_data.user_id
    entity['phase_num'] = user_data.phase_num
    entity['accepting_text'] = user_data.accepting_text
    entity['campaign_name'] = user_data.campaign_name

    return entity