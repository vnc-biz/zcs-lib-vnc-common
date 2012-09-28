package biz.vnc.zimbra.extension;

import biz.vnc.zimbra.util.ZLog;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.extension.ExtensionException;
import com.zimbra.cs.extension.ZimbraExtension;
import com.zimbra.cs.mailbox.Conversation;
import com.zimbra.cs.mailbox.Document;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailboxListener;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.session.PendingModifications;
import com.zimbra.cs.session.PendingModifications.ModificationKey;

/**
 * abstract mailboxd notify handler
 *
 * analyzes the events and passes them to specific handler functions
 */
public abstract class MailboxNotifyHandler
	extends MailboxListener
		implements ZimbraExtension {

	public abstract String getName();

	public void init() throws ExtensionException, ServiceException {
		register(this);
	}

	public void destroy() {
	}

	public void handleCreate(String accountId, ModificationKey key, OperationContext ctx, Conversation conversation, int lastChangeId) {
	}

	public void handleCreate(String accountId, ModificationKey key, OperationContext ctx, Document document, int lastChangeId) {
	}

	public void handleCreate(String accountId, ModificationKey key, OperationContext ctx, Folder folder, int lastChangeId) {
	}

	public void handleCreate(String accountId, ModificationKey key, OperationContext ctx, Message message, int lastChangedId) {
	}

	public void handleCreate(String accountId, PendingModifications mods, OperationContext ctx, int lastChangeId) {
		if (mods.created == null)
			return;

for (ModificationKey mk : mods.created.keySet()) {
			Object o = mods.created.get(mk);
			if (o instanceof Message) {
				handleCreate(accountId, mk, ctx, (Message)o, lastChangeId);
			} else if (o instanceof Folder) {
				handleCreate(accountId, mk, ctx, (Folder)o, lastChangeId);
			} else if (o instanceof Document) {
				handleCreate(accountId, mk, ctx, (Document)o, lastChangeId);
			} else if (o instanceof Conversation) {
				handleCreate(accountId, mk, ctx, (Conversation)o, lastChangeId);
			} else {
				ZLog.warn(getName(), "handleCreate: object of unknown type: "+o.toString());
			}
		}
	}

	public void handleModify(String accountId, ModificationKey key, OperationContext ctx, Conversation document, int lastChangeId) {
	}

	public void handleModify(String accountId, ModificationKey key, OperationContext ctx, Document document, int lastChangeId) {
	}

	public void handleModify(String accountId, ModificationKey key, OperationContext ctx, Folder folder, int lastChangeId) {
	}

	public void handleModify(String accountId, ModificationKey key, OperationContext ctx, Message message, int lastChangedId) {
	}

	public void handleModify(String accountId, PendingModifications mods, OperationContext ctx, int lastChangeId) {
		if (mods.modified == null)
			return;

for (ModificationKey mk : mods.modified.keySet()) {
			Object o = mods.modified.get(mk);
			if (o instanceof Message) {
				handleModify(accountId, mk, ctx, (Message)o, lastChangeId);
			} else if (o instanceof Conversation) {
				handleModify(accountId, mk, ctx, (Conversation)o, lastChangeId);
			} else if (o instanceof Folder) {
				handleModify(accountId, mk, ctx, (Folder)o, lastChangeId);
			} else if (o instanceof Document) {
				handleCreate(accountId, mk, ctx, (Document)o, lastChangeId);
			} else {
				ZLog.warn(getName(), "handleModify: object of unknown type: "+o.toString());
			}
		}
	}

	public void handleDelete(String accountId, PendingModifications mods, OperationContext ctx, int lastChangeId) {
		if (mods.deleted == null)
			return;

for (ModificationKey mk : mods.deleted.keySet()) {
			Object o = mods.deleted.get(mk);
		}
	}

	@Override
	public void notify(MailboxListener.ChangeNotification notification) {
		handleMailboxChange(
		    notification.mailboxAccount.getId(),
		    notification.mods,
		    notification.ctxt,
		    notification.lastChangeId
		);
	}

	public void handleMailboxChange(String accountId, PendingModifications mods, OperationContext ctx, int lastChangeId) {
		handleCreate(accountId, mods, ctx, lastChangeId);
		handleModify(accountId, mods, ctx, lastChangeId);
		handleDelete(accountId, mods, ctx, lastChangeId);
	}
}
