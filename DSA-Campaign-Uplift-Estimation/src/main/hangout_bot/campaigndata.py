class CampaignData:
    def __init__(self, owner):
      self.owner = owner
      self.name = ''
      self.start_date = ''
      self.end_date = ''
      self.daily_budget = 0.0
      self.manual_CPC = 0.0
      self.locations = ''
      self.neg_locations = ''
      self.domain = ''
      self.targets = ''
      self.ad_text = ''

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
    
    def set_ad_text(self, ad_text):
        self.ad_text = ad_text