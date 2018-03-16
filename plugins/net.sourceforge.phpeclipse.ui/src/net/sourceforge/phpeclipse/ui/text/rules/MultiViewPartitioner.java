/*
 * Copyright (c) 2002-2004 Widespace, OU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://solareclipse.sourceforge.net/legal/cpl-v10.html
 * 
 * Contributors:
 *     Igor Malinin - initial contribution
 * 
 * $Id: MultiViewPartitioner.java,v 1.10 2006-10-21 23:13:53 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.ui.text.rules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.IDocumentPartitioningListenerExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

/**
 * Advanced partitioner which maintains partitions as views to connected
 * document. Views have own partitioners themselves. This class is designed as a
 * base for complex partitioners such as for JSP, PHP, ASP, etc. languages.
 * 
 * @author Igor Malinin
 */
public abstract class MultiViewPartitioner extends AbstractPartitioner {

	class ViewListener implements IDocumentPartitioningListener,
			IDocumentPartitioningListenerExtension {

		/*
		 * @see org.eclipse.jface.text.IDocumentPartitioningListener#documentPartitioningChanged(IDocument)
		 */
		public void documentPartitioningChanged(IDocument document) {
			IDocumentView view = (IDocumentView) document;

			int start = view.getParentOffset(0);
			int end = view.getParentOffset(view.getLength());

			rememberRegion(start, end - start);
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentPartitioningListenerExtension#documentPartitioningChanged(IDocument,
		 *      IRegion)
		 */
		public void documentPartitioningChanged(IDocument document,
				IRegion region) {
			IDocumentView view = (IDocumentView) document;

			int offset = region.getOffset();

			int start = view.getParentOffset(offset);
			int end = view.getParentOffset(offset + region.getLength());

			rememberRegion(start, end - start);
		}
	}

	private ViewListener viewListener = new ViewListener();

	private OuterDocumentView outerDocument;

	private DocumentEvent outerDocumentEvent;

	public MultiViewPartitioner(IPartitionTokenScanner scanner) {
		super(scanner);
	}

	public void setOuterPartitioner(IDocumentPartitioner partitioner) {
		if (outerDocument == null) {
			if (partitioner == null) {
				return;
			}

			outerDocument = new OuterDocumentView(document, nodes);
			outerDocument.addDocumentPartitioningListener(viewListener);
		}

		IDocumentPartitioner old = outerDocument.getDocumentPartitioner();
		if (old != null) {
			outerDocument.setDocumentPartitioner(null);
			old.disconnect();
		}

		if (partitioner != null) {
			partitioner.connect(outerDocument);
		}

		outerDocument.setDocumentPartitioner(partitioner);

		if (partitioner == null) {
			outerDocument.removeDocumentPartitioningListener(viewListener);
			outerDocument = null;
		}
	}

	/**
	 * Create subpartitioner.
	 * 
	 * @param contentType
	 *            name of inner partition or <code>null</code> for outer
	 *            partition
	 */
	protected abstract IDocumentPartitioner createPartitioner(String contentType);

	protected void addInnerRegion(FlatNode position) {
		if (outerDocument != null) {
			if (DEBUG) {
				Assert.isTrue(position.offset >= 0, Integer
						.toString(position.offset));
			}
			int outerOffset = outerDocument.getLocalOffset(position.offset);
			// axelcl start
			DocumentEvent event = null;
			if (outerOffset >= 0) {
				// axelcl end
				event = new DocumentEvent(outerDocument, outerOffset,
						position.length, null);

				outerDocument.fireDocumentAboutToBeChanged(event);
			}
			super.addInnerRegion(position);
			// axelcl start
			if (event != null) {
				// axelcl end
				outerDocument.fireDocumentChanged(event);
			}
		} else {
			super.addInnerRegion(position);
		}

		if (position instanceof ViewNode) {
			// TODO: revisit condition
			IDocumentPartitioner partitioner = createPartitioner(position.type);
			if (partitioner != null) {
				InnerDocumentView innerDocument = new InnerDocumentView(
						document, (ViewNode) position);

				((ViewNode) position).view = innerDocument;

				partitioner.connect(innerDocument);
				innerDocument.setDocumentPartitioner(partitioner);
				innerDocument.addDocumentPartitioningListener(viewListener);
			}
		}
	}

	protected void removeInnerRegion(FlatNode position) {
		try {
			if (outerDocument != null) {
				DocumentEvent event = null;
				if (position.offset >= 0 && position.length >= 0) {
					int outerOffset = outerDocument
							.getLocalOffset(position.offset);
					if (outerOffset > 0) {
						event = new DocumentEvent(outerDocument, outerOffset,
								0, document.get(position.offset,
										position.length));

						outerDocument.fireDocumentAboutToBeChanged(event);
					}
				}
				super.removeInnerRegion(position);
				if (position.offset >= 0) {
					if (event != null) {
						outerDocument.fireDocumentChanged(event);
					}
				}
			} else {
				super.removeInnerRegion(position);
			}

			if (position instanceof ViewNode) {
				// TODO: revisit condition
				InnerDocumentView innerDocument = ((ViewNode) position).view;
				if (innerDocument != null) {
					IDocumentPartitioner partitioner = innerDocument
							.getDocumentPartitioner();

					innerDocument
							.removeDocumentPartitioningListener(viewListener);
					innerDocument.setDocumentPartitioner(null);
					partitioner.disconnect();
				}
			}
		} catch (BadLocationException e) {
		}
	}

	protected void deleteInnerRegion(FlatNode position) {
		super.deleteInnerRegion(position);

		if (position instanceof ViewNode) {
			// TODO: revisit condition
			InnerDocumentView innerDocument = ((ViewNode) position).view;
			if (innerDocument != null) {
				IDocumentPartitioner partitioner = innerDocument
						.getDocumentPartitioner();

				innerDocument.removeDocumentPartitioningListener(viewListener);
				innerDocument.setDocumentPartitioner(null);
				partitioner.disconnect();
			}
		}
	}

	public void connect(IDocument document) {
		// outerDocument = new OuterDocumentView(document, innerPositions);

		super.connect(document);

		setOuterPartitioner(createPartitioner(null));
		// IDocumentPartitioner partitioner =
		// partitioner.connect(outerDocument);
		// outerDocument.setDocumentPartitioner(partitioner);
		// outerDocument.addDocumentPartitioningListener(viewListener);
	}

	public void disconnect() {
		try {
			if (outerDocument != null) {
				outerDocument.removeDocumentPartitioningListener(viewListener);

				IDocumentPartitioner partitioner = outerDocument
						.getDocumentPartitioner();

				outerDocument.setDocumentPartitioner(null);
				partitioner.disconnect();
			}
		} finally {
			// TODO: cleanup listeners
			outerDocument = null;
		}
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentPartitioner#documentAboutToBeChanged(DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
		super.documentAboutToBeChanged(event);

		outerDocumentEvent = null;

		int offset = event.getOffset();
		int length = event.getLength();
		int end = offset + length;

		// find left partition
		int first = computeFlatNodeIndex(offset);
		if (first > 0) {
			FlatNode p = (FlatNode) nodes.get(first - 1);

			int right = p.offset + p.length;
			if (offset < right) {
				// change overlaps with partition
				InnerDocumentView innerDocument = null;
				if (p instanceof ViewNode) {
					// TODO: revisit condition
					innerDocument = ((ViewNode) p).view;
				}

				if (end < right) {
					if (innerDocument != null) {
						// cahnge completely inside partition
						int start = innerDocument.getLocalOffset(offset);
						innerDocument
								.fireDocumentAboutToBeChanged(new DocumentEvent(
										innerDocument, start, length, event
												.getText()));
					}

					return;
				}

				if (innerDocument != null) {
					// cut partition at right
					int start = innerDocument.getLocalOffset(offset);
					innerDocument
							.fireDocumentAboutToBeChanged(new DocumentEvent(
									innerDocument, start, innerDocument
											.getLength()
											- start, null));
				}
			}
		}

		// find right partition
		int last = computeFlatNodeIndex(end);
		if (last > 0) {
			FlatNode p = (FlatNode) nodes.get(last - 1);

			if (p instanceof ViewNode) {
				// TODO: revisit condition
				InnerDocumentView innerDocument = ((ViewNode) p).view;
				if (innerDocument != null) {
					int right = p.offset + p.length;
					if (end < right) {
						// cut partition at left
						int cut = innerDocument.getLocalOffset(end);
						innerDocument
								.fireDocumentAboutToBeChanged(new DocumentEvent(
										innerDocument, 0, cut, null));
					}
				}
			}
		}

		if (outerDocument != null) {
			int left = outerDocument.getLocalOffset(offset);
			int right = outerDocument.getLocalOffset(end);

			String text = event.getText();

			if (left >= 0 && (right - left >= 0)) {
				if (left != right || text != null && text.length() > 0) {
					outerDocumentEvent = new DocumentEvent(outerDocument, left,
							right - left, text);

					outerDocument
							.fireDocumentAboutToBeChanged(outerDocumentEvent);
				}
			}
		}
	}

	protected int fixupPartitions(DocumentEvent event) {
		int offset = event.getOffset();
		int length = event.getLength();
		int end = offset + length;

		// fixup/notify inner views laying on change boundaries

		int first = computeFlatNodeIndex(offset);
		if (first > 0) {
			FlatNode p = (FlatNode) nodes.get(first - 1);

			int right = p.offset + p.length;
			if (offset < right) {
				// change overlaps with partition
				if (end < right) {
					// cahnge completely inside partition
					String text = event.getText();
					p.length -= length;
					if (text != null) {
						p.length += text.length();
					}

					if (p instanceof ViewNode) {
						// TODO: revisit condition
						InnerDocumentView innerDocument = ((ViewNode) p).view;
						if (innerDocument != null) {
							int start = innerDocument.getLocalOffset(offset);
							innerDocument
									.fireDocumentChanged(new DocumentEvent(
											innerDocument, start, length, text));
						}
					}
				} else {
					// cut partition at right
					int cut = p.offset + p.length - offset;
					p.length -= cut;

					if (p instanceof ViewNode) {
						// TODO: revisit condition
						InnerDocumentView innerDocument = ((ViewNode) p).view;
						if (innerDocument != null) {
							int start = innerDocument.getLocalOffset(offset);
							// TODO: ???fireDocumentAboutToBeChanged???
							innerDocument
									.fireDocumentChanged(new DocumentEvent(
											innerDocument, start, cut, null));
						}
					}
				}
			}
		}

		int last = computeFlatNodeIndex(end);
		if (last > 0 && first != last) {
			FlatNode p = (FlatNode) nodes.get(last - 1);

			int right = p.offset + p.length;
			if (end < right) {
				// cut partition at left
				int cut = end - p.offset;
				p.length -= cut;
				p.offset = offset;

				String text = event.getText();
				if (text != null) {
					p.offset += text.length();
				}

				if (p instanceof ViewNode) {
					// TODO: revisit condition
					InnerDocumentView innerDocument = ((ViewNode) p).view;
					if (innerDocument != null) {
						// TODO: ???fireDocumentAboutToBeChanged???
						innerDocument.fireDocumentChanged(new DocumentEvent(
								innerDocument, 0, cut, null));
					}
				}

				--last;
			}
		}

		// fixup inner views laying afrer change

		String text = event.getText();
		if (text != null) {
			length -= text.length();
		}

		for (int i = last, size = nodes.size(); i < size; i++) {
			((FlatNode) nodes.get(i)).offset -= length;
		}

		// delete inner views laying completely inside change boundaries

		if (first < last) {
			do {
				deleteInnerRegion((FlatNode) nodes.get(--last));
			} while (first < last);

			rememberRegion(offset, 0);
		}

		// notify outer view

		if (outerDocumentEvent != null) {
			outerDocument.fireDocumentChanged(outerDocumentEvent);
		}

		return first;
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentPartitioner#computePartitioning(int,
	 *      int)
	 */
	protected String getContentType(String parent, String view) {
		if (view != null) {
			return view;
		}

		if (parent != null) {
			return parent;
		}

		return IDocument.DEFAULT_CONTENT_TYPE;
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentPartitioner#computePartitioning(int,
	 *      int)
	 */
	public ITypedRegion[] computePartitioning(int offset, int length) {
		List list = new ArrayList();
		// if (DEBUG) {
		// if (length>=9400) {
		// length--;
		// }
		// System.out.print("MultiViewPartitioner::computePartitioning - Offset:
		// ");
		// System.out.print(offset);
		// System.out.print(", Length: ");
		// System.out.print(length);
		// System.out.println("");
		// }
		int end = offset + length;

		int index = computeFlatNodeIndex(offset);
		while (true) {
			FlatNode prev = (index > 0) ? (FlatNode) nodes.get(index - 1)
					: null;

			if (prev != null) {
				if (prev.overlapsWith(offset, length)) {
					addInnerPartitions(list, offset, length, prev);
				}

				if (end <= prev.offset + prev.length) {
					break;
				}
			}

			FlatNode next = (index < nodes.size()) ? (FlatNode) nodes
					.get(index) : null;

			if (next == null || offset < next.offset) {
				addOuterPartitions(list, offset, length, prev, next);
			}

			if (next == null) {
				break;
			}

			++index;
		}
		// if (DEBUG) {
		// showList(list);
		// }

		return (TypedRegion[]) list.toArray(new TypedRegion[list.size()]);
	}

	private void showList(List list) {
		try {
			throw new NullPointerException();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(">>>>>List start");
		TypedRegion temp;
		for (int i = 0; i < list.size(); i++) {
			temp = (TypedRegion) list.get(i);
			System.out.print("Offset: ");
			System.out.print(temp.getOffset());
			System.out.print(", Length: ");
			System.out.print(temp.getLength());
			System.out.print(", Type: ");
			System.out.print(temp.getType());
			System.out.println("");
		}
		System.out.println("<<<<<List end");
	}

	private void addOuterPartitions(List list, int offset, int length,
			FlatNode prev, FlatNode next) {
		// limit region
		int start = offset;
		int end = offset + length;

		if (prev != null && start < prev.offset + prev.length) {
			start = prev.offset + prev.length;
		}

		if (next != null && next.offset < end) {
			end = next.offset;
		}

		if (start == end) {
			return;
		}

		if (outerDocument == null) {
			// if (DEBUG) {
			// if (end - start<0) {
			// throw new IndexOutOfBoundsException();
			// }
			// }
			list.add(new TypedRegion(start, end - start, getContentType(null,
					IDocument.DEFAULT_CONTENT_TYPE)));
			return;
		}

		try {
			// convert to outer offsets
			start = outerDocument.getLocalOffset(start);
			end = outerDocument.getLocalOffset(end);
			int len = end - start;
			if (len >= 0) {
				ITypedRegion[] regions = null;
				try {
					regions = outerDocument.computePartitioning(start, len);
				} catch (Exception e) {
					// nasty workaround, which prevents cursor from moveing
					// backwards in the editor
					// but doesn't solve the partitioning problem
					regions = new ITypedRegion[0];
					System.out
							.println("MultiViewerPartitioner#addOuterPartitions failure\n"
									+ "start:"
									+ start
									+ " length:"
									+ len
									+ "\n" + outerDocument.get(start, len));
				}
				for (int i = 0; i < regions.length; i++) {
					ITypedRegion region = regions[i];

					// convert back to parent offsets
					start = outerDocument.getParentOffset(region.getOffset());
					end = start + region.getLength();

					if (prev != null) {
						offset = prev.offset + prev.length;
						if (start < offset) {
							start = offset;
						}
					}

					if (next != null) {
						offset = next.offset;
						if (offset < end) {
							end = offset;
						}
					}
					// if (DEBUG) {
					// if (end - start<0) {
					// showList(list);
					// System.out.print("MultiViewPartitioner::addOuterPartitions
					// - Offset: ");
					// System.out.print(offset);
					// System.out.print(", Start: ");
					// System.out.print(start);
					// System.out.print(", End: ");
					// System.out.print(end);
					// System.out.print(", Type: ");
					// System.out.print(region.getType());
					// System.out.println("");
					// throw new IndexOutOfBoundsException();
					// }
					// }
					list.add(new TypedRegion(start, end - start,
							getContentType(null, region.getType())));
				}
			}
		} catch (BadLocationException x) {
		}
	}

	private void addInnerPartitions(List list, int offset, int length,
			FlatNode position) {
		InnerDocumentView innerDocument = null;
		if (position instanceof ViewNode) {
			// TODO: revisit condition
			innerDocument = ((ViewNode) position).view;
		}

		if (innerDocument == null) {
			// simple partition
			// if (DEBUG) {
			// if (position.length<0) {
			// throw new IndexOutOfBoundsException();
			// }
			// }
			list.add(new TypedRegion(position.offset, position.length,
					getContentType(position.type, null)));
			return;
		}

		// multiplexing to inner view
		try {
			// limit region
			int start = Math.max(offset, position.offset);
			int end = Math.min(offset + length, position.offset
					+ position.length);

			// convert to document offsets
			length = end - start;
			offset = innerDocument.getLocalOffset(start);

			ITypedRegion[] regions = innerDocument.computePartitioning(offset,
					length);

			for (int i = 0; i < regions.length; i++) {
				ITypedRegion region = regions[i];

				// convert back to parent offsets
				offset = innerDocument.getParentOffset(region.getOffset());
				length = region.getLength();
				// if (DEBUG) {
				// if (length<0) {
				// throw new IndexOutOfBoundsException();
				// }
				// }
				list.add(new TypedRegion(offset, length, getContentType(
						position.type, region.getType())));
			}
		} catch (BadLocationException x) {
		}
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getPartition(int)
	 */
	public ITypedRegion getPartition(int offset) {
		if (nodes.size() == 0) {
			return getOuterPartition(offset, null, null);
		}

		int index = computeFlatNodeIndex(offset);
		if (index < nodes.size()) {
			FlatNode next = (FlatNode) nodes.get(index);

			if (offset == next.offset) {
				return getInnerPartition(offset, next);
			}

			if (index == 0) {
				return getOuterPartition(offset, null, next);
			}

			FlatNode prev = (FlatNode) nodes.get(index - 1);

			if (prev.includes(offset)) {
				return getInnerPartition(offset, prev);
			}

			return getOuterPartition(offset, prev, next);
		}

		FlatNode prev = (FlatNode) nodes.get(nodes.size() - 1);

		if (prev.includes(offset)) {
			return getInnerPartition(offset, prev);
		}

		return getOuterPartition(offset, prev, null);
	}

	protected ITypedRegion getOuterPartition(int offset, FlatNode prev,
			FlatNode next) {
		try {
			int start, end;
			String type;

			if (outerDocument == null) {
				start = 0;
				end = document.getLength();
				type = getContentType(null, IDocument.DEFAULT_CONTENT_TYPE);
			} else {
				int outerOffset = outerDocument.getLocalOffset(offset);
				// axelcl start
				if (outerOffset < 0) {
					start = 0;
					end = document.getLength();
					type = getContentType(null, IDocument.DEFAULT_CONTENT_TYPE);
				} else {
					// axelcl end
					ITypedRegion region = outerDocument
							.getPartition(outerOffset);

					start = region.getOffset();
					end = start + region.getLength();

					// convert to parent offset
					start = outerDocument.getParentOffset(start);
					end = outerDocument.getParentOffset(end);

					type = getContentType(null, region.getType());
				}
			}

			if (prev != null) {
				offset = prev.offset + prev.length;
				if (start < offset) {
					start = offset;
				}
			}

			if (next != null) {
				offset = next.offset;
				if (offset < end) {
					end = offset;
				}
			}

			return new TypedRegion(start, end - start, type);
		} catch (BadLocationException x) {
			x.printStackTrace();
			throw new IllegalArgumentException();
		}
	}

	protected ITypedRegion getInnerPartition(int offset, FlatNode position) {
		if (position instanceof ViewNode) {
			// TODO: revisit condition
			InnerDocumentView innerDocument = ((ViewNode) position).view;

			if (innerDocument != null) {
				// multiplexing to inner view
				try {
					// convert to inner offset
					ITypedRegion region = innerDocument
							.getPartition(innerDocument.getLocalOffset(offset));

					// convert to parent offset
					offset = innerDocument.getParentOffset(region.getOffset());

					return new TypedRegion(offset, region.getLength(),
							getContentType(position.type, region.getType()));
				} catch (BadLocationException x) {
				}
			}
		}

		// simple partition
		return new TypedRegion(position.offset, position.length, position.type);
	}
}