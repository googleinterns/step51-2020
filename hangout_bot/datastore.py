from google.cloud import datastore
from campaigndata import CampaignData
from userdata import *
from constant import *

# [Datastore Interaction Functions]

def get_user_key(user_id):
    return datastore.Client().key('ActiveUser', user_id)

def get_campaign_key(user_id, campaign_name):
    return datastore.Client().key('CampaignData', '{}{}'.format(user_id, campaign_name))

def add_new_campaign(user_id, campaign_name):
    """Adds newly created campaign to datastore
    Args:
      user_id:
        The user associated with the new campaign
      campaign_name:
        The name of the newly added campaign
    Returns:
      None
    """

    new_campaign = CampaignData(user_id)
    new_campaign.set_name(campaign_name)
    new_campaign = convert_campaign_to_entity(new_campaign)
    put_entity_in_datastore(new_campaign)

def update_campaign_data(campaign_data, phase_num, value):
    """Modifies CampaignData class parameters based on phase_num
       and value function parameters
    Args:
      campaign_data:
        The CampaignData class being modified
      phase_num:
        The phase_num of the parameter being modified
      value:
        The new value being set in the CampaignData class object
    Returns:
      CampaignData
        Modified version of class
    """

    if (phase_num == NAME):
        campaign_data.set_start_date(value)
    elif (phase_num == START_DATE):
        campaign_data.set_end_date(value)
    elif (phase_num == END_DATE):
        campaign_data.set_daily_budget(value)
    elif (phase_num == LOCATIONS):
        campaign_data.set_locations(value)
    elif (phase_num == NEG_LOCATIONS):
        campaign_data.set_neg_locations(value)
    elif (phase_num == DOMAIN):
        campaign_data.set_domain(value)
    elif (phase_num == TARGETS):
        campaign_data.set_targets(value)
    elif (phase_num == MANUAL_CPC):
        campaign_data.set_manual_CPC(value)
    elif (phase_num == AD_TEXT):
        campaign_data.set_ad_text(value)
    return campaign_data

def add_campaign_data(campaign_data):
    """Adds CampaignData type to datastore
    Args:
      campaign_data: 
        The CampaignData object that is prepared and added to datastore
        as CampaignData entity
    Returns:
      None
    """

    campaign_entity = convert_campaign_to_entity(campaign_data)
    put_entity_in_datastore(campaign_entity)

def add_new_user(event):
    """Initializes and adds user data in datastore based on passed in event
    Args:
      event:
        The event containing user information to be added to datastore
    Returns:
      None
    """

    user_data = ActiveUser(event['user']['email'])
    user_entity = convert_user_to_entity(user_data)
    put_entity_in_datastore(user_entity)

def put_entity_in_datastore(entity):
    datastore.Client().put(entity)

def update_user(user_data):
    """Updates user data in datastore with the passed in data
    Args:
      user_data:
        The data used to generate a key and entity to be put in datastore
    Returns:
      None
    """

    user_entity = convert_user_to_entity(user_data)
    put_entity_in_datastore(user_entity)

def get_user_data(user_key):
    """Returns entity specific to the key
    Args:
      user_key:
        The key used to retrieve data from datastore
    Yields:
      UserData:
        Parsed user data associated with key
    """

    user_entity = datastore.Client().get(user_key)
    return convert_entity_to_user(user_entity)

def get_campaign_data(campaign_key):
    """Returns campaign entity specific to the key
    Args:
      user_key:
        The key used to retrieve data from datastore
    Yields:
      Entity
        contains UserData, requires parsing to be used by bot
    """

    return convert_entity_to_campaign(datastore.Client().get(campaign_key))

def get_user_campaigns(user_id):
    query = datastore.Client().query(kind='CampaignData')
    result = query.add_filter('owner', '=', user_id).fetch()
    return list(result)

def delete_datastore_entity(key):
    datastore.Client().delete(key)

# [Datastore and class conversion functions]

def convert_entity_to_campaign(campaign_entity):
    """Returns campaign specific to entity
    Args:
      campaign_entity:
        Entity to be converted into CampaignData
    Yields:
      None
        if entity is not valid
      CampaignData
        parsed CampaignData object from entity
    """
    
    if campaign_entity == None: 
        return None
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
    """Returns entity converted from campaign parameter
    Args:
      campaign:
        Campaign to be converted into entity
    Yields:
      Entity
        parsed Entity CampaignData object
    """

    # Create the entity key
    entity_key = get_campaign_key(campaign.owner, campaign.name)

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

def convert_entity_to_user(user_entity):
    """Returns user specific to entity
    Args:
      user_entity:
        Entity to be converted into UserData
    Yields:
      None
        if entity is not valid
      UserData
        parsed UserData object from entity
    """    

    if entity == None: 
        return None
    user_data = ActiveUser(entity['user'])
    
    user_data.set_accepting_text(user_entity['accepting_text'])
    user_data.set_campaign_name(user_entity['campaign_name'])
    user_data.set_phase_num(user_entity['phase_num'])
    user_data.set_editing(user_entity['editing'])
    return user_data

def convert_user_to_entity(user_data):
    """Returns entity specific to UserData
    Args:
      user_data:
        UserData to be converted to datastore entity
    Yields:
      Entity
        Converted entity object from UserData
    """    

    entity_key = get_user_key(user_data.user_id)

    user_entity = datastore.Entity(entity_key)

    user_entity['user'] = user_data.user_id
    user_entity['phase_num'] = user_data.phase_num
    user_entity['accepting_text'] = user_data.accepting_text
    user_entity['campaign_name'] = user_data.campaign_name
    user_entity['editing'] = user_data.editing

    return user_entity