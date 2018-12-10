Simple web crawler

Problem Statement :-  Design a simple web crawler. The crawler should be limited to one domain. Given a starting URL – say http://www.prudential.co.uk/  - it should visit all pages within the domain, but not follow the links to external sites such as Google or Twitter.

The output should be a simple structured site map (this does not need to be a traditional XML sitemap - just some sort of output to reflect what your crawler has discovered), showing links to other pages under the same domain, links to external URLs and links to static content such as images for each respective page.


How to run :- 

1) Clone this repository in local machine (git clone ..)
2) maven build , mvn clean install 
3) java -jar target/crawler-1.0-SNAPSHOT.jar " http://www.prudential.co.uk/"

or Alternatively 

If you dont want to run without commnad line, you can use any IDE and run as Crawler java class and provide the arguments " http://www.prudential.co.uk/" in run configurations.


Improvement Areas:-

1) We can add max depth for any link to process, otherwise it will keep crawling forever.
2) Some link generate dynamic link, in some cases it redirected to same page, causing Loop and will never finish
3) Retry logic if some resource is not available at that point of time
4) Deduplication if already processed.
5) Scalable via making this as dockerize component and deployed in cluster
6) We can do better in not crawling outside domain resources.
