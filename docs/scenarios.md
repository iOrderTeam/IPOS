Checklist for the functionality of the products
The list of features that will be checked during the final demonstration are listed below:
IPOS-SA sub-system
IPOS-SA-ACC package
◼ creating/changing/deleting a user account
◼ ability to create new user accounts (login/password) and assign roles (access privileges)
◼ users should be allowed to do only things defined by their access privileges and not allowed to do things which
exceed their privileges.
◼ change roles (promote/demote) users to different level of access.
◼ This can be done via deleting an existing user account and creating it with different privileges.
◼ creating/changing/deleting a merchant account
◼ setting up a credit limit and a discount plan for a merchant account
◼ maintaining account state (from “normal” to “suspended” and eventually to “in default”) including reactivating
accounts “in default” to “normal”
IPOS-SA-CAT package
◼ adding/deleting/modifying a product in the catalogue
◼ setting/changing minimum stock availability.
◼ search in the catalogue by product ID, keyword, etc.
◼ adding new stock (i.e. increasing stock availability)
◼ warnings for stock below the set lower bound (e.g. when a stock item is open for modification, or when a user logs
in)
IPOS-SA-ORD package
◼ accepting new orders (submitted from IPOS CA),
◼ generating invoices and storing them on SA.
◼ managing the order status (from “accepted” to “ready to dispatch”, then to “dispatched”, then to delivered).
◼ monitoring current balance of a merchant.
◼ Recording payments from merchants using various forms of payment (e.g. bank transfers).
◼ Recording payments may change the merchant account status from “suspended” to “normal” provided the
outstanding balance from previous month has been cleared.
◼ observing the list of orders taken but not completed.
IPOS-SA-RPT package
◼ ability to generate all reports required with an option to print them.
◼ generating a file (e.g. a pdf file) will be considered equivalent to printing.
◼ generating reminders to merchant debtors (shown on CA screen) every time a merchant debtor tries to log in.
IPOS-CA sub-system
IPOS-CA-ORD package
◼ placing new orders with SA
◼ checking progress with SA order
◼ search of SA orders and viewing invoices.
◼ Check status of own merchant account.
◼ (optional) record merchant account details on CA for ease of access to SA by different users of IPOS - CA.
IPOS-CA-USER package
◼ create/change/delete an IPOS-CA user account with different privileges.
IPOS-CA-CUST package
◼ create/change/delete an IPOS-CA customer account.
◼ set up/modify a credit limit.
◼ set up/modify a discount plan for an existing account holder (customer).
◼ generate reminders to debtors (1st and 2nd reminders)
◼ maintain customer account (from “normal” to “suspended” and then to “in default”) including reactivation of an
account “in default” by an authorised user, provided the debt from the previous month has been cleared.
IPOS-CA-Templates package
◼ Ability to create/modify reminder templates (1st and 2nd reminder).
◼ Make sure that the templates are used in any future reminders.
IPOS-CA-Stock package
◼ dealing with local stock:
◼ adding new stock (a new item and increasing the quantity of an existing stock item)
◼ dealing with order deliveries (when an IPOS-SA order is delivered)1
◼ listing (on request) all items, which are below their respective lower stock bounds.
IPOS-CA-Sales package
◼ process purchases by customers, produce retail invoices.
◼ take payments from customers using various forms of payment (including cash and credit card)
◼ maintain orders received via the PU portal (order status will be changed from “accepted” to “ready for shipment”,
then to “shipped” and finally “delivered”).
IPOS-CA-RPT package
◼ ability to generate all reports required with option to print them.
◼ generating a file (e.g. a pdf file) will be considered equivalent to printing.
IPOS-PU sub-system
IPOS-PU-Members package
◼ application for non-commercial account (membership)
◼ application for commercial account (membership)
◼ login/ by non-commercial members
◼ non-commercial customer can view their purchase history.
◼ ability by non-commercial members to track their orders (see the state of orders).
IPOS-PU-Sales package
◼ show IPOS - CA catalogue.
◼ show a list of links to active promotion campaigns (provided there is at least one active campaign at the time a
customer is scanning the on-line catalogue)
◼ clicking the link of an active promotion campaign will show details on promotion campaign.
◼ maintain a shopping cart for customers (and non-commercial members)
◼ apply discounts to products included in active promotion campaigns.
◼ maintain all counters envisaged in the initial statement of requirements (when links/product items are
selected/added to cart/paid for).
◼ checkout
◼ accounting for non-commercial customer getting a 10% discount from every tenth order.
◼ Collect delivery address.
◼ Collect online payment.
◼ Send the customer a sale confirmation with an order tracking link.
1 An assumption can be made that all orders placed with IPOS-SA are delivered as placed (i.e. the delivered items and
quantities are as specified in the order).
IPOS-PU-Comms package
◼ (optional) Configure access to SMTP server.
◼ (optional) Configure access to an online payment processor.
◼ Sending mails on behalf of IPOS-SA
◼ Processing credit card payment requests on behalf of IPOS-CA (card payments are processed via the online
payment processor)
IPOS-PU-PRM package
◼ Create/modify/delete/terminate early promotion campaigns.
◼ Setting period of campaign
◼ Including items from IPOS-CA catalogue
◼ Setting discount rates
IPOS-PU-RPT package
◼ Create all reports envisaged in the initial requirements documents with option to print them.
◼ generating a file (e.g. a pdf file) will be considered equivalent to printing.
G: General functionality
◼ Sensible search
◼ Cascaded deletes/updates
◼ GUI – appearance of the forms (intuitive, easy to use) and consistency between the forms.

Sample Data
The data provided below should be used to populate the databases of IPOS-SA, IPOS-CA, and IPOS – PU subsystems.
Prior to populating the databases, you MUST remove all the data from the database of the respective subsystem, i.e.
leave all tables blank, with a single exception – a single (default) login which will allow you to create the required login
usernames/passwords.

IPOS_SA subsystem
Logins
The following user account must be active with IPOS-SA:
Username Password role
Sysdba London_weighting Administrator
manager Get_it_done Director of Operations
accountant Count_money Senior accountant
clerk Paperwork Accountant
warehouse1 Get_a_beer Warehouse employee
warehouse2 Lot_smell Warehouse employee
delivery Too_dark Delivery department employee
Account details of merchants registered with InfoPharma
Account Holder Name: CityPharmacy
Account No: ACC0001
Contact Name: Prof David Rhind
Address: Northampton Square, London EC1V 0HB
Phone: 0207 040 8000
Credit Limit: £10,000
Agreed Discount: Fixed
Discount Rate: 3%
login: city
password: northampton
Account Holder Name: Cosymed Ltd
Account No: ACC0002
Contact Name: Mr Alex Wright
Address: 25, Bond Street, London WC1V 8LS
Phone: 0207 321 8001
Credit Limit: £5,000
Agreed Discount: Variable (on volume per month)
Discount Rate: Volume Discount Rate
< £1000 : 0 %
£1000 - £2000: 1%
£2000+ : 2 %
login: cosymed
password: bondstreet
Account Holder Name: HelloPharmacy
Account No: ACC0003
Contact Name: Mr Bruno Wright
Address: 12, Bond Street, London WC1V 9NS
Phone: 0207 321 8002
Credit Limit: £5,000
Agreed Discount: Variable (on volume per month)
Discount Rate: Volume Discount Rate
< £1000 : 0 %
£1000 - £2000: 1%
£2000+: 3 %
login: hello
password: there
InfoPharma catalogue
Item ID Description Package
Type
Unit Units in a
pack
Package
Cost, £
Availability, packs Stock limit, packs
100 00001 Paracetamol box Caps 20 0.10 10,345 300
100 00002 Aspirin box Caps 20 0.50 12, 453 500
100 00003 Analgin box Caps 10 1.20 4,235 200
100 00004 Celebrex, caps 100 mg box Caps 10 10.00 3,420 200
100 00005 Celebrex, caps 200 mg box caps 10 18.50 1,450 150
100 00006 Retin-A Tretin, 30 g box caps 20 25.00 2,013 200
100 00007 Lipitor TB, 20 mg box caps 30 15.50 1,562 200
100 00008 Claritin CR, 60g box caps 20 19.50 2,540 200
200 00004 Iodine tincture bottle ml 100 0.30 2,2134 200
200 00005 Rhynol bottle ml 200 2.50 1,908 300
300 00001 Ospen box caps 20 10.50 809 200
300 00002 Amopen box caps 30 15.00 1340 300
400 00001 Vitamin C box caps 30 1.20 3,258 300
400 00002 Vitamin B12 box caps 30 1.30 2,673 300
IPOS_CA subsystem
The following user account must be active with the version of IPOS-CA deployed at Cosymed Ltd.:
Username Password Role
sysdba masterkey Administrator
manager Get_it_done Director of Operations/Manager
accountant Count_money Senior accountant
clerk Paperwork Accountant
Customers registered as account holders with Cosymed Ltd.
Account Holder Name: Ms Eva Bauyer
Account No: ACC0001
Contact Name: Ms Eva Bauyer
Address: 1, Liverpool street, London EC2V 8NS
Phone: 0207 321 8001
Credit Limit: £500
Agreed Discount: Fixed: 3%
Account Holder Name: Mr Glynne Morrison
Account No: ACC0002
Contact Name: Ms Glynne Morisson
Address: 1, Liverpool street, London EC2V 8NS
Phone: 0207 321 8001
Credit Limit: £500
Agreed Discount: Variable (on volume per month)
Discount Rate: Volume Discount Rate
< £100 : 0 %
£100 - £300: 1%
£300+ : 2 %
Stock availability at Cosymed Ltd
Item ID Description Package
Type
Unit Units in a
pack
Package
Cost, £
Availability, packs Stock limit, packs
100 00001 Paracetamol Box Caps 20 0.10 121 10
100 00002 Aspirin Box Caps 20 0.50 201 15
100 00003 Analgin Box Caps 10 1.20 25 10
100 00004 Celebrex, caps 100 mg Box Caps 10 10.00 43 10
100 00005 Celebrex, caps 200 mg Box Caps 10 18.50 35 5
100 00006 Retin-A Tretin, 30 g Box Caps 20 25.00 28 10
100 00007 Lipitor TB, 20 mg Box Caps 30 15.50 10 10
100 00008 Claritin CR, 60g Box Caps 20 19.50 21 10
200 00004 Iodine tincture Bottle Ml 100 0.30 35 10
200 00005 Rhynol Bottle Ml 200 2.50 14 15
300 00001 Ospen Box Caps 20 10.50 78 10
300 00002 Amopen Box Caps 30 15.00 90 15
400 00001 Vitamin C Box Caps 30 1.20 22 15
400 00002 Vitamin B12 Box Caps 30 1.30 43 15
Retail prices are calculated with 100% mark up to the unit cost. 0.0% VAT is applied to the retail price.
IPOS_PU subsystem
The following user accounts must be active with the version of IPOS-PU linked to merchant Cosymed Ltd:
Username Password Role
sysdba masterkey Administrator
manager GetPU_it_done PU-Admin
The following membership applications were made via IPOS-PU (Cosymed Ltd):
Non-commercial Member
Account: PU0001
Email address: cool@example.com2
password: 12ss_56_SS
Non-commercial Member
Account No: PU0002
Email address: cool1@example.com3
password: 34pp_78_LL
Commercial Member
Account No: PU0003
Company Name: Pond Pharmacy
Address: Chislehurst
25, High Street
BR7 5BN
Company House Registration: UK10003429CompH
Email address: pondPharma@example.com4
2 You can amend this email to a valid email address, so that emails can be sent to it.
3 You can amend this email to a valid email address, so that emails can be sent to it.
4 You can amend this email to a valid email address, so that emails can be sent to it.
Scenarios
1. On 20 February 2026 CityPharmacy, placed the following order with IPOS-SA:
   Item ID Description Quantity Unit Cost, £ Total, £
   100 00001 Paracetamol 10 0.10 1.00
   100 00003 Analgin 20 1.20 24.00
   200 00004 Iodine tincture 20 0.30 3.60
   200 00005 Rhynol 10 2.50 25.00
   300 00001 Ospen 10 10.50 105.00
   300 00002 Amopen 20 15.00 300.00
   400 00001 Vitamin C 20 1.20 24.00
   400 00002 Vitamin B12 20 1.30 26.00
   Grand Total: 508.60
   The goods were delivered on 23 February 2026, 15:00 to the merchant’s premises by the InfoPharma’s own courier
   service.
2. On 25 February 2026, the merchant Cosymed Ltd, placed the following order with IPOS-SA:
   Item ID Description Quantity Unit Cost, £ Total, £
   100 00001 Paracetamol 10 0.10 1.00
   100 00003 Analgin 20 1.20 24.00
   200 00005 Rhynol 10 2.50 25.00
   300 00002 Amopen 20 15.00 300.00
   400 00002 Vitamin B12 20 1.30 26.00
   Grand Total: 376.00
   The goods were delivered on 26 February, 17:00 to the merchant’s premises by the DHL.
3. On 25 February 2026, the merchant HelloPharmacy placed the following order with IPOS - SA:
   Item ID Description Quantity Unit Cost, £ Total, £
   100 00003 Analgin 20 1.20 24.00
   200 00004 Iodine tincture 20 0.30 3.60
   300 00001 Ospen 3 10.50 31.50
   300 00002 Amopen 10 15.00 150.00
   400 00001 Vitamin C 20 1.20 24.00
   400 00002 Vitamin B12 20 1.30 26.00
   Grand Total: 259.10
   The goods were delivered on 27 February 2026, 10:00 to the merchant’s premises by the DHL.
4. On 10 March 2026, the merchant Cosymed Ltd placed the following order with IPOS - SA:
   Item ID Description Quantity Unit Cost, £ Total, £
   200 00005 Rhynol 10 2.50 25.00
   300 00001 Ospen 10 10.50 105.00
   300 00002 Amopen 20 15.00 300.00
   Grand Total: 430.00
   The goods were delivered on 12 March, 11:00 to the merchant’s premises by the InfoPharma’s own courier service.
5. On 25 March 2026, the merchant HelloPharmacy placed the following order with IPOS - SA:
   Item ID Description Quanti
   ty
   Unit Cost,
   £
   Total, £
   100 00003 Analgin 20 1.20 24.00
   100 00004 Celebrex, caps 100 mg 5 10.00 50.00
   100 00005 Celebrex, caps 200 mg 5 18.50 92.50
   100 00006 Retin-A Tretin, 30 g 5 25.00 125.00
   100 00007 Lipitor TB, 20 mg 10 15.50 155.00
   300 00001 Ospen 10 10.50 105.00
   300 00002 Amopen 20 15.00 300.00
   400 00002 Vitamin B12 20 1.30 26.00
   Grand Total: 877.50
   The goods were delivered on 27 March, 10:00 to the merchant’s premises by the InfoPharma’s own courier service.
6. On 1 April 2026, the merchant HelloPharmacy placed the following order with IPOS - SA:
   Item ID Description Quanti
   ty
   Unit Cost,
   £
   Total, £
   100 00003 Analgin 20 1.20 24.00
   100 00004 Celebrex, caps 100 mg 5 10.00 50.00
   100 00005 Celebrex, caps 200 mg 5 18.50 92.50
   100 00006 Retin-A Tretin, 30 g 5 25.00 125.00
   100 00007 Lipitor TB, 20 mg 10 15.50 155.00
   300 00001 Ospen 10 10.50 105.00
   400 00002 Vitamin B12 20 1.30 26.00
   Grand Total: 577.50
   The goods were delivered on 3 April 2026, 10:00 to the merchant’s premises by the InfoPharma’s own courier
   service.
7. HelloPharmacy have made no payments since 5 March 2026 when they cleared their balance.
8. CityPharmacy have made a full payment on 15 March 2026 and cleared their balance using a bank transfer to the
   account of InfoPharma.
9. Cosymed Ltd have made a full payment on 15 March 2026 and cleared their balance using company’s credit
   card.
10. On 1 March 2026 Eva Bauyer bought the following goods from Cosymed Ltd:
    Ospen 1 Boxes
    Vitamin C 2 Boxes
    Amopen 2 Boxes
    Vitamin B12 2 Boxes
11. On 3 March 2026, the following purchases of goods were made from the pharmacy shops of Cosymed Ltd (i.e.
    via the IPOS-CA subsystem) by several cash customers:
    a)
    Aspirin 2 Boxes
    Analgin 3 Boxes
    For goods, the customer paid cash.
    b)
    Celebrex, caps 100 mg 2 Boxes
    Retin-A Tretin, 30 g 2 Boxes
    Paid with credit card Visa.
    c)
    Lipitor TB, 20 mg 1 Box
    Claritin CR, 60g 1 Box
    Paid cash.
    d)
    Celebrex, caps 200 mg 1 Box
    Iodine tincture 2 Bottles
    Rhynol 2 Bottles
    Paid with cash.
    e)
    Ospen 2 Boxes
    Vitamin C 2 Boxes
    Paid with debit card.
    f)
    Amopen 3 Boxes
    Vitamin B12 2 Boxes
    Paid cash.
12. On 5 March 2026 Glynne Morisson bought from a Cosymed Ltd Pharmacy Shop Ltd the following goods:
    Aspirin 2 Boxes
    Analgin 3 Boxes
    Celebrex, caps 100 mg 2 Boxes
    Retin-A Tretin, 30 g 2 Boxes
    The cost of goods was added to Glynne’s account with Cosymed Ltd.
13. On 1 April Eva Bauyer bought the following goods from a Cosymed Pharmacy shop:
    Ospen 1 Boxes
    Analgin 3 Boxes
    Celebrex, caps 100 mg 2 Boxes
    Vitamin B12 2 Boxes
    The cost of goods was added to Eva’s account with Cosymed Ltd.
14. Glynne Morisson paid on 29 March the full balance with a credit card.
15. The last payment from Eva Bauyer was received on 30 February.
16. On the 15 April reminding letters were generated and sent to all debtors. The same must be done on 29 April.
17. On the 15 March 2026, the Admin – PU user of IPOS-PU created a promotion campaign as follows:
    Name: March Promotion
    Valid from: 15 March 2026
    Valid to: 20 April 2026
    Item Discount rate
    Aspirin 5%
    Analgin 10%
    Celebrex, caps 100 mg 10%
    Retin-A Tretin, 30 g 20%
18. On the 30 March 2026, the Admin – PU user of IPOS-PU created another promotion campaign as follows:
    Name: April Promotion
    Valid from: 5 April 2026
    Valid to: 10 April 2026
    Item Discount rate
    Ospen 20%
    Vitamin C 10%
19. On the 20 of March 2026 Peter Popov scanned the Cosymed Ltd online catalogue (via the PU subsystem),
    clicked on the link of “March Promotion”. Then from the list of the promotion campaigns, he added two items to
    the shopping cart:
- Aspirin - 1 box
- Retin-A Tretin, 30 g – 1 box.
  He then paid using his AmEx credit card: 0000 000000 0000 0001, security code 3245, valid until 30/08/2030.
  The payment has been successful. Peter was sent an email confirming the sale and with a link to track the delivery.
20. Between 1st and 20th of March 2026 the non-commercial customer PU0001 (see login details above) has made 8
    online purchases of various goods (details of these have not been stored, but the number of purchases (8) has been
    stored by the PU). On the 8th of April 2026 PU0001 logged in again and checked the active promotion campaigns.
    Then they added Ospen – 1 box to the shopping cart. They checked out and paid for the goods using their
    Mastercard 0001 0002 0003 0005 with security code 032 and expiry date 9/2028.
    The payment transaction has been successful and PU0001 received an email confirming the purchase with an order
    tracking link included in the email.
    Created: 25 February 2026
    Last revised 20 March 2026
    Version 1.0