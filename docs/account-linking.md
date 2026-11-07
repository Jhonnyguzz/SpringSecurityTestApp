# Account linking: current behavior, risks and options

SongBox lets users sign in with three methods: form login (email + password),
Google (OIDC) and GitHub (OAuth2). All of them end up in a single `AppUser`
identified by email. User creation lives in `service/UserService`.

## Current behavior

| # | Scenario | Result | Covered by |
|---|----------|--------|------------|
| 1 | Register with the form using an email that already exists | Rejected ("Ya existe una cuenta con este correo") | `UserServiceTests`, manual check |
| 2 | Sign in with a provider whose email already belongs to a local account | Signs in to the existing account; the local account is **not** modified (provider stays `LOCAL`, password kept, form login still works) | `OAuth2UserUnificationTests#githubLoginLinksToExistingLocalAccount` |
| 3 | Sign in with provider 2 when provider 1 already created the account | Signs in to the existing account; the stored provider stays the first one | `OAuth2UserUnificationTests#secondProviderReusesAccountCreatedByFirstProvider` |
| 4 | Register with the form using an email created through a provider | Rejected | `UserServiceTests#registerLocalRejectsEmailAlreadyUsedByExternalAccount` |

Provider emails are trusted only when verified: Google requires `email_verified`
and GitHub uses the primary verified address from `/user/emails`.

## Risk: pre-account takeover

Form registration does **not** verify that the user owns the email. Combined with
scenario 2, this allows an attacker to take over an account before its owner
creates it:

1. The attacker registers `victim@gmail.com` with the form and a password they know.
2. Later, the victim signs in with Google and is linked to that same account.
3. The victim uses the account (adds songs, data, ...). The attacker can still sign
   in with their password and access everything.

Scenarios 3 and 4 are safe because both sides of the link are verified emails.

## Options

### Option 1 – Verify the email on form registration (recommended)

- New local accounts are created with `emailVerified = false` and a single-use,
  expiring token. A `/verify?token=...` link is sent by email (or logged in
  development; a tool like Mailpit can be added later).
- `AppUserPrincipal.isEnabled()` returns `emailVerified`. `DaoAuthenticationProvider`
  then throws `DisabledException` automatically, and `/login` can show
  "Please verify your email".
- OAuth2 users are created as verified.
- Combine with option 3 for the remaining case: if a provider signs in to an
  **unverified** local account, mark it verified and discard the local password.

Pros: fixes the root cause; industry standard; shows more Spring Security
features (`isEnabled`, `DisabledException`, custom failure messages).
Cons: requires a token entity/flow and, eventually, an email sender.

#### Provider sign-in when a local account with the same email exists

**Case A – local account verified** (the user clicked the link):

- The provider login is linked to the existing account, as today.
- The local password is kept, so both form login and the provider work.
- Safe: both sides proved ownership of the email.

**Case B – local account not verified:**

The account may belong to the user (registered but never clicked the link) or to
an attacker who used their email. The app cannot tell, but the provider has just
proved that the person signing in owns the email, so:

1. The user signs in to that account through the provider.
2. The account is marked as verified.
3. The local password is discarded, because nobody knows who set it.
4. To use form login again, the user defines a new password through a
   "set/reset password" flow.

If the password was set by an attacker, they lose access. If it was the user,
they only need to create a new password.

| Local account | Provider sign-in | Local password |
|---|---|---|
| Verified | Linked | Kept |
| Not verified | Linked and verified | Discarded |
| Does not exist | Account created (verified) | None |

### Option 2 – Do not auto-link unverified local accounts

When a provider signs in with an email that belongs to an unverified local
account, reject the login, or ask the user to confirm by entering the local
password before linking.

Pros: no automatic linking of untrusted accounts.
Cons: worse UX, extra screens, and it still leaves accounts with someone else's
email in the database. Only meaningful together with option 1.

### Option 3 – Wipe the local password when linking

When a provider signs in to an existing local account, set its password to `null`
(the provider just proved ownership of the email). The legitimate owner can
define a new password later through a "set/reset password" flow.

Pros: simplest change (a few lines in `UserService.findOrCreateExternal`); kills
the attacker's access.
Cons: on its own, legitimate users lose their password whenever they first use a
provider; unverified accounts still exist until someone links them.

## Other real-world improvements

- **Scenario 4 UX:** instead of a plain rejection, offer "sign in with Google/GitHub"
  or a "set/forgot password" flow so provider users can add a password.
- **User enumeration:** "An account with this email already exists" reveals which
  emails are registered. Real apps often show a neutral message and send an email
  instead.
- **Identity table:** link providers through a `user_identity (user_id, provider,
  subject)` table using the provider's stable subject id (`sub` / GitHub `id`)
  rather than the email, which the user can change at the provider. This also
  records every linked provider instead of only the first one.
- **Persistent OAuth2 clients:** only needed if the app calls provider APIs after
  login (refresh tokens, `JdbcOAuth2AuthorizedClientService`). Not needed for
  login only.
