# 3.6.0
* Respond with the single "best" location fix rather than each enabled
  type.
* "Last-Known" location provider types now also supply the time that the
  location was found.
* Fix: Gracefully handle multiple location requests. Previously only the
  latest would be responded to.
* A message indicating that the location cannot be found is sent if all
  location providers fail.

# 3.5.1
* Fix: Changed release configuration to avoid all preferences being lost due
  to minify bug.

# 3.5.0
* Added names to recipients
* Button to send SMS with correct prefix from main activity
* Fix: Manually entering a contact's number without the country code
  prevents incoming SMSs being detected.
* Fix: Preventing crash when contact provider doesn't return valid phone
  number.
