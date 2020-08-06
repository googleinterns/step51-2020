# DSA Campaign Uplift Estimation

### Project Overview
This project addresses two main problems with the current Dynamic Search Ads framework. The first involves the lack of transparency associated with automated products and the limited predictability of DSA and machine learning in general. Users have no guarantee that using DSA will produce the results they intend, creating some risk for users attempting to transition over from their traditional keyword campaigns to DSA. The second issue involves the sometimes unnecessary complexity of DSA. Especially for newer advertisers and smaller companies, incorporating DSA into their already confusing keyword campaigns only adds another layer of complexity.

To address these issues, we built a web app that allows advertisers to run test DSA campaigns and estimate the uplift DSA can bring to their keyword campaigns. Users can experiment with different DSA campaign settings when creating their campaigns, and easily view their campaign results on the home page and compare the results of their different configurations. There is also an additional Google Hangouts Bot that allows users to perform all of the same functions on the website but in a step-by-step, chat-based context. By allowing users to clearly see the value DSA can bring to their ad campaigns, this project hopes to convince more users to incorporate DSA into their ad campaigns.

This project is a model of what DSA could eventually implement. Many changes would have to be made for this project to be used in production, such as a more accurate estimation feature, a cleaner, simpler UI, and auto-generated campaign settings using AI.

### User Experience
When users access the site, they first have to login in order to view their created DSA campaigns. When they access the home page, they will see information on all of their previously created DSA campaigns in the form of charts and graphs. Three test keyword campaigns have been created so that all users can interact with the UI and experiment with creating their own DSA campaigns. If implemented in production, the tool would pull keyword campaigns from a user's existing Google Ads account.

The create page is where users create and run DSA campaigns. After a user inputs all the required settings and runs the campaign, the estimation results will be able to be viewed on the home page.

The compare page allows users to easily compare the results of their different DSA campaigns. Users can select any number of their created DSA campaigns to compare, and the uplift statistics will be displayed along with the inputted campaign settings and generated search query reports.

### Estimation Algorithm
The estimation process takes into account all of the factors inputted on the create page, as well as the settings of the associated keyword campaign. The estimation process does not resemble the way DSA works in production - various formulas are used to factor in the settings to obtain the rough estimates. The parts of the process that involve the most complexity are generating the locations and website factors. To obtain the locations factor, the algorithm uses 2019 US Census data population estimates to analyze the number of people targeted in the DSA campaign versus that of the keyword campaign. This relative difference is used in the final estimation calculations.

To create a website factor for the provided domain and target page links, the algorithm implements a web crawler to analyze the target pages and many of the pages linked to on the domain. The algorithm performs a keyword analysis on pages to search for a common theme or thread of ideas. While performing the keyword analyses, the algorithm also generates the SQR as a by-product.

### Demo Video
https://www.youtube.com/watch?time_continue=1&v=3CFrmuMjKig&feature=emb_logo

### Contact
For any questions/feedback, please send an email to one of the following depending on the nature of your inquiry:

Nevin George (nevingeorge2000@gmail.com) - created the compare page and developed the estimation algorithm.\
Neel Bagora (neelbagora@gmail.com) - created the create page and Hangouts Bot.\
Erik Garcia (erikgarciam246@gmail.com) - created the login and home pages.

### References
We used 2019 US Census Population Estimates in our calculations.\
Link: https://www.census.gov/data/tables/time-series/demo/popest/2010s-state-total.html#par_textimage_1574439295
