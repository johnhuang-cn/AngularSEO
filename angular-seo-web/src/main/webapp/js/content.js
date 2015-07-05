angular.module('AngularSEOApp', [])
    .controller('ContentController', function() {
        var content = this;
        content.description = "Free Java filter make AngularJS + Java EE site crawlable by ALL search engines";

        // about section
        content.aboutTitle = "How It Works?";
        content.aboutDetail = "AngularSEO Filter is a SEO solution for AJAX frond-end sites with a JavaEE backend. The AJAX frond-end can be AngularJS, ReactJS and other AJAX frameworkd.";

        content.aboutTitle1 = "Integrate SEO filter on JEE server";
        content.aboutDetail1 = "Add a filter into web.xml, and set its URL mapping to '/*'.";

        content.aboutTitle2 = "Prerender static page snapshots";
        content.aboutDetail2 = "When server started, SEO filter will crawl self site and generate static page snapshot for each one with all the dynamic content fully rendered. It will update the snapshots regularly according your config.";

        content.aboutTitle3 = "Let crawlers see static content";
        content.aboutDetail3 = "SEO filter checks the User-Agent keyword to identify the crawl request. If it is Googlebot, Bingbot, etc, filter will response with the pre-rendered static page from the snapshot.";

        content.aboutTitle4 = "All hashbang URL are well transformed";
        content.aboutDetail4 = "All hashbang URLs will be transformed to normal format which can be crawled more easily. So, you needn't care any speical rule of hashbang URL crawling specification.";

        // feature section
        content.feature1 = "Easy setup: configure a filter only";
        content.feature2 = "Zero code change: needn't change any front-end and backend codes";
        content.feature3 = "Fast page loads: snapshot is prerendered & stored on the same server";
        content.feature4 = "Fully automated: it keeps working in the background after setup.";
        content.feature5 = "It is FREE!";
        content.feature6 = "It is open source!";
        
        // next plan section
        content.nextPlan1 = "This site is a simple AngularJS site integrated with AngularSEO filter, now it can be crawled by search engines correctly. You can search 'angularseo.net' in Google or Bing, and check the cache page.";
        content.nextPlan2 = "I will continue poshing it and do more tests with complicated AJAX sites.";
    });